package gb.cloud.server;

import gb.cloud.common.network.User;
import gb.cloud.server.db.DBMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
Keeps clint's context
* */
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

    private void setPath(User user) throws IOException{
        this.username = user.getLogin();
        setUserPath(ServerSettings.FILE_DIRECTORY + username + "/");
        if(Files.notExists(userPath)){
            Files.createDirectories(userPath);
        }
    }

    public boolean login(User user) throws IOException {
        isAuthorized = db.loginUser(user);
        if(isAuthorized){
            setPath(user);
        }
        return isAuthorized;
    }

    public boolean register(User user) throws IOException {
        boolean ok = db.registerUser(user);
        if(ok){
            setPath(user);
        }
        return ok;
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
