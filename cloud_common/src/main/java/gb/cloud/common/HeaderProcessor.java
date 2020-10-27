package gb.cloud.common;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.User;
import gb.cloud.common.password.Password;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HeaderProcessor {
    private final String fileDirectory;

    public HeaderProcessor(String fileDirectory){
        this.fileDirectory = fileDirectory;
    }

    public CommandMessage processHeader(JSONObject header) {
        String command = (String)header.get(CommonSettings.J_COMMAND);
        CommandMessage cMessage = new CommandMessage(Command.valueOf(command));
        JSONObject fileEntry;

        switch (cMessage.getCommand()){
            case LOGIN:
            case REGISTER:
                cMessage.setUser(new User((String)header.get(CommonSettings.J_USERNAME), Password.getHash((String)header.get(CommonSettings.J_PASSWORD))));
                break;
            case SEND_FILE:
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFileSize((long)fileEntry.get(CommonSettings.J_SIZE));
                cMessage.setFilePath(Paths.get(fileDirectory + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case PULL_FILE:
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFilePath(Paths.get(fileDirectory + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case SEND_TREE:
                fileEntry = (JSONObject)header.get(CommonSettings.J_LIST);
                cMessage.setFileTree(fileEntry);
                break;
        }

        return cMessage;
    }

}
