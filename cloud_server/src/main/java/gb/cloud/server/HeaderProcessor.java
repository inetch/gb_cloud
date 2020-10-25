package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import org.json.simple.JSONObject;

public class HeaderProcessor {
    public static boolean processHeader(JSONObject header){
        String command = (String)header.get(CommonSettings.J_COMMAND);
        String userName;
        String userPassword;

        switch (command){
            case CommonSettings.C_AUTH:
                userName = (String)header.get(CommonSettings.J_USERNAME);
                userPassword = (String)header.get(CommonSettings.J_PASSWORD);
                return Authorizer.auth(userName, userPassword);

            case CommonSettings.C_REG:
                userName = (String)header.get(CommonSettings.J_USERNAME);
                userPassword = (String)header.get(CommonSettings.J_PASSWORD);
                return Authorizer.register(userName, userPassword);
        }

        return true;
    }
}
