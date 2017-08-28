package com.okta.examples.zorkoauth.service;

import com.google.common.base.Stopwatch;
import com.okta.examples.zorkoauth.model.CommandResponse;
import com.okta.examples.zorkoauth.support.ZMachinery;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserProfile;
import com.zaxsoft.zmachine.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class GameServiceImpl implements GameService {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    @Value("#{ @environment['zmachine.profile.attribute.name'] ?: 'zMachineData' }")
    String zMachineProfileAttributeName;

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    @Override
    public String getSaveFile(User user) {
        return saveFilePath + File.separator + user.getId() + ".sav";
    }

    @Override
    public void restart(User user) {
        UserProfile userProfile = user.getProfile();
        userProfile.put(zMachineProfileAttributeName, "");
        user.update();
    }

    @Override
    public void loadGameState(StringBuffer zMachineCommands, User user) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // retrieve existing game (if any) from profile
        String zMachineSaveData = (String) user.getProfile().get(zMachineProfileAttributeName);

        stopwatch.stop();
        log.info("time to load zMachine save data from customData: " + stopwatch);

        if (zMachineSaveData != null) {
            stopwatch = Stopwatch.createStarted();

            // save data to file to be restored in game before sending new command
            byte[] rawData = Base64.getDecoder().decode(zMachineSaveData);
            Path p = FileSystems.getDefault().getPath("", getSaveFile(user));
            Files.write(p, rawData);

            stopwatch.stop();
            log.info("time to write zMachine save data to file: " + stopwatch);

            // setup restore command if we had profile data
            zMachineCommands.append("restore\n");
        }
    }

    @Override
    public void saveGameState(User user) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // store save file in custom data
        Path p = FileSystems.getDefault().getPath("", getSaveFile(user));

        byte [] fileData = Files.readAllBytes(p);
        String saveFile = Base64.getEncoder().encodeToString(fileData);
        user.getProfile().put(zMachineProfileAttributeName, saveFile);
        user.update();

        stopwatch.stop();
        log.info("time to save zMachine save data to customData: " + stopwatch);
        log.info("Wrote to file: " + getSaveFile(user));
    }

    @Override
    public String doZMachine(StringBuffer zMachineCommands, User user) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        log.info("executing: " + zMachineCommands.toString().replace("\n", ", ") +
            " for: " + user.getProfile().getEmail());

        String fileName = getSaveFile(user);

        // setup zmachine
        InputStream in = new ByteArrayInputStream(zMachineCommands.toString().getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Monitor monitor = new Monitor();
        ZMachinery zMachinery = new ZMachinery(zFile, in, out, fileName, monitor);

        // ensure that we are done writing based on the number of input commands
        int numOutput = zMachineCommands.toString().split("\n").length + 1;
        for (int i=0; i<numOutput; i++) {
            synchronized (in) {
                try {
                    in.wait(500);
                } catch (InterruptedException e) {
                    log.error("Interrupted: {}", e.getMessage(), e);
                }
            }
        }

        // get anything from zMachine in buffer
        String res = out.toString();

        if (zMachineCommands.indexOf("save\n") >= 0) {
            // ensure save file is written before killing zmachine
            monitor.doWait();
        }

        // kill zmachine
        zMachinery.quit();

        stopwatch.stop();
        log.info("time to fire up and execute zMachine commands: " + stopwatch);

        return res;
    }

    @Override
    public CommandResponse processZMachineResponse(String zMachineRequest, String zMachineResponse) {
        // process output
        // get rid of "OK"s and prompts
        zMachineResponse = zMachineResponse.replace("Ok.\n\n>", "")
            .replace(">", "");

        String[] tmpResponse = zMachineResponse.split("\n\n");

        int rLength = tmpResponse.length;

        String[] gameInfo = tmpResponse[0].split("\n");
        String[] look = (rLength < 3) ? tmpResponse[1].split("\n") : tmpResponse[2].split("\n");

        String[] response = (rLength >= 4) ? tmpResponse[3].split("\n") : null;

        // get response from zmachine
        CommandResponse res = new CommandResponse();
        res.setGameInfo(gameInfo);
        res.setLook(look);
        res.setRequest(zMachineRequest);
        res.setResponse(response);
        res.setStatus("SUCCESS");

        return res;
    }

    @Override
    public void cleanup(User user) throws IOException {
        Path p = FileSystems.getDefault().getPath("", getSaveFile(user));
        Files.deleteIfExists(p);
    }
}
