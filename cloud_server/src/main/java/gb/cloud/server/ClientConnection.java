package gb.cloud.server;

import gb.cloud.server.db.DBMain;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConnection {
    private boolean isAuthorized;
    private String username;
    private Path userPath;

    public Path getUserPath() {
        return userPath;
    }

    public void setUserPath(Path userPath) {
        this.userPath = userPath;
    }

    public void setUserPath(String userPathString) {
        this.userPath = Paths.get(userPathString);
    }

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
        if(isAuthorized){
            setUserPath(ServerSettings.FILE_DIRECTORY + username + "/");
        }
    }
}
