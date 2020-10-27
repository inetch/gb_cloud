package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.User;
import gb.cloud.common.password.Password;
import org.json.simple.JSONObject;

import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class HeaderProcessor {
    public static CommandMessage processHeader(JSONObject header) {
        String command = (String)header.get(CommonSettings.J_COMMAND);
        CommandMessage cMessage = new CommandMessage(Command.valueOf(command));

        switch (cMessage.getCommand()){
            case LOGIN:
            case REGISTER:
                cMessage.setUser(new User((String)header.get(CommonSettings.J_USERNAME), Password.getHash((String)header.get(CommonSettings.J_PASSWORD))));
                break;
            case SEND_FILE:
                JSONObject fileEntry = (JSONObject)header.get(CommonSettings.J_FILE);
                cMessage.setFileSize((long)fileEntry.get(CommonSettings.J_SIZE));
                cMessage.setFilePath(Paths.get(ServerSettings.FILE_DIRECTORY + fileEntry.get(CommonSettings.J_FILENAME)));
                break;
        }

        return cMessage;
    }
}
