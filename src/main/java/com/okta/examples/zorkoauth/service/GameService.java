package com.okta.examples.zorkoauth.service;

import com.okta.examples.zorkoauth.model.CommandResponse;
import com.okta.sdk.resource.user.User;

import java.io.IOException;

public interface GameService {

    String getSaveFile(User user);
    void restart(User user);
    void loadGameState(StringBuffer zMachineCommands, User user) throws IOException;
    void saveGameState(User user) throws IOException;
    String doZMachine(StringBuffer zMachineCommands, User user);
    CommandResponse processZMachineResponse(String zMachineRequest, String zMachineResponse);
    void cleanup(User user) throws IOException;
}
