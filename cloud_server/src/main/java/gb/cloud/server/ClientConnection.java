package gb.cloud.server;

import gb.cloud.common.network.User;

public class ClientConnection {
    private boolean isAuthorized;
    private String username;

    public ClientConnection(){
        this.isAuthorized = false;
    }

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
    }

    public boolean isAuthorized(){
        return this.isAuthorized;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
