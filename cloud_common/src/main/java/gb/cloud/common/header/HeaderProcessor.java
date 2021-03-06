package gb.cloud.common.header;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ICommandMessage;
import gb.cloud.common.network.User;
import org.json.simple.JSONObject;

import java.nio.file.Paths;

/*Deserialization from JSON to CommandMessage
* */
public class HeaderProcessor {
    public static ICommandMessage processHeader(JSONObject header, String rootDirectory) {
        System.out.println("Process header");
        System.out.println(header);

        String command = (String)header.get(CommonSettings.J_COMMAND);
        ICommandMessage cMessage = new CommandMessage(Command.valueOf(command));
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
                if(header.containsKey(CommonSettings.J_TREE)){
                    cMessage.setFileTree((JSONObject)header.get(CommonSettings.J_TREE));
                }
                break;
            case PUSH_FILE: //to server
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFileSize((long)fileEntry.get(CommonSettings.J_SIZE));

                String folder;
                if(header.containsKey(CommonSettings.J_FOLDER)){
                    folder = rootDirectory + (String)header.get(CommonSettings.J_FOLDER);
                }else{
                    folder = rootDirectory;
                }

                cMessage.setFilePath(Paths.get(folder + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case PULL_FILE:
                fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                if(fileEntry.containsKey(CommonSettings.J_SIZE)) {
                    cMessage.setFileSize((long) fileEntry.get(CommonSettings.J_SIZE));
                }
                cMessage.setFilePath(Paths.get(rootDirectory + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
            case PULL_TREE: //tree request from client
                break;
            case PUSH_TREE: //response with tree from server
                cMessage.setFileTree((JSONObject)header.get(CommonSettings.J_TREE));
                break;
            case CREATE_DIR:
                if(header.containsKey(CommonSettings.J_FOLDER)) {
                    cMessage.setTargetFolder((String) header.get(CommonSettings.J_FOLDER));
                }
                break;
        }

        return cMessage;
    }

}
