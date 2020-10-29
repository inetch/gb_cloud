package gb.cloud.common;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.User;
import gb.cloud.common.password.Password;
import org.json.simple.JSONObject;

import java.nio.file.Paths;

public class HeaderProcessor {
    private final String fileDirectory;

    public HeaderProcessor(String fileDirectory){
        this.fileDirectory = fileDirectory;
    }

    public CommandMessage processHeader(JSONObject header) {
        String command = (String)header.get(CommonSettings.J_COMMAND);
        CommandMessage cMessage = new CommandMessage(Command.valueOf(command));
        if(header.containsKey(CommonSettings.J_RESULT)){
            cMessage.setResult((boolean)header.get(CommonSettings.J_RESULT));
        }else{
            cMessage.setResult(true);
        }

        JSONObject fileEntry;

        switch (cMessage.getCommand()){
            case LOGIN:
            case REGISTER:
                User user = new User();
                if(header.containsKey(CommonSettings.J_USERNAME)){
                    user.setLogin((String)header.get(CommonSettings.J_USERNAME));
                }
                if(header.containsKey(CommonSettings.J_PASSWORD)){
                    user.setPassword((String)header.get(CommonSettings.J_PASSWORD));
                }
                cMessage.setUser(user);
                break;
            case PUSH_FILE: //to server
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFileSize((long)fileEntry.get(CommonSettings.J_SIZE));
                cMessage.setFilePath(Paths.get(fileDirectory + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case PULL_FILE:
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFilePath(Paths.get(fileDirectory + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case PULL_TREE: //tree request from client
                break;
            case PUSH_TREE: //response with tree from server
                cMessage.setFileTree((JSONObject)header.get(CommonSettings.J_LIST));
                break;
        }

        return cMessage;
    }

}
