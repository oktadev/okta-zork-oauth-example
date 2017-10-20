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
import java.io.IOException;
import java.security.Principal;

import static com.okta.examples.zorkoauth.OktaSpringBootOauthExampleApplication.VERSION;

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
