package com.okta.examples.zorkoauth.controller;

import com.okta.examples.zorkoauth.model.CommandRequest;
import com.okta.examples.zorkoauth.model.CommandResponse;
import com.okta.examples.zorkoauth.service.GameService;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static com.okta.examples.zorkoauth.config.SpringSecurityWebAppConfig.VERSION;

@RestController
public class GameController {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

//    @RequestMapping("/")
//    public void root(HttpServletResponse res) throws IOException {
//        res.sendRedirect(VERSION +"/instructions");
//    }

    @RequestMapping(VERSION + "/instructions")
    public CommandResponse home(HttpServletRequest req) {
        String proto = (req.getHeader("x-forwarded-proto") != null) ?
                req.getHeader("x-forwarded-proto") : req.getScheme() ;
        String server = req.getServerName();
        String port = (req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort();
        String baseUrl = proto + "://" + server + port;

        CommandResponse res = new CommandResponse();

        String[] response = {
                "Welcome to the interactive OAuth2 Text Based Adventure!",
                "",
                "In order to play the game, you must:",
                "    1. Register an account",
                "    2. Get an access token using your account",
                "    3. Use the access token to send commands to the game",
                "",
                "To use the access token to interact with the game, you send a POST request to the command endpoint (the below example uses httpie):",
                "    http POST " + baseUrl + "/v1/c Authorization:'Bearer <access token>'",
                "    http POST " + baseUrl + "/v1/c command='go north' Authorization:'Bearer <access token>'",
                "Note: if you don't send the command parameter, the response will contain the result of looking around your current location in the game",
                "",
                "Part of the game is discovering which language elements work to move you forward in the game.",
                "If you are impatient, here's a list of all the available commands: http://zork.wikia.com/wiki/Command_List"
        };

        res.setResponse(response);
        res.setStatus("SUCCESS");

        return res;
    }

    @RequestMapping(value = VERSION + "/game", method = RequestMethod.POST)
    public @ResponseBody CommandResponse command(
        @RequestBody(required = false) CommandRequest commandRequest,
        HttpServletRequest req,
        Principal principal
    ) throws IOException {
        Client client = Clients.builder().build();
        User user = client.getUser(principal.getName());

        String zMachineRequest = (commandRequest != null) ? commandRequest.getCommand() : null;
        StringBuffer zMachineCommands = new StringBuffer();

        //check for restart
        if ("restart".equals(zMachineRequest)) {
            gameService.restart(user);
            zMachineRequest = null;
        } else {
            // restore games state from customData, if it exists
            gameService.loadGameState(zMachineCommands, user);
        }

        // we always want to look
        zMachineCommands.append("look\n");

        // setup passed in command
        if (zMachineRequest != null) {
            zMachineCommands.append(zMachineRequest + "\n");
            zMachineCommands.append("save\n");
        }

        // execute game move (which may just be looking)
        String zMachineResponse = gameService.doZMachine(zMachineCommands, user);

        CommandResponse res = gameService.processZMachineResponse(zMachineRequest, zMachineResponse);

        if (zMachineRequest != null) {
            gameService.saveGameState(user);
        }

        gameService.cleanup(user);

        // return response
        return res;
    }

}
