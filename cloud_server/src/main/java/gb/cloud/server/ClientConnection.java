package gb.cloud.server;

import gb.cloud.server.db.DBMain;

public class ClientConnection {
    private boolean isAuthorized;
    private String username;

    public DBMain getDb() {
        return db;
    }

    private final DBMain db;

    public ClientConnection(DBMain db){
        this.isAuthorized = false;
        this.db = db;
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
