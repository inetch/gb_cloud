package gb.cloud.server;

import gb.cloud.common.network.User;
import gb.cloud.server.db.IDBMain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
Keeps clint's context
* */
public class ClientConnection implements IClientConnection{
    private boolean isAuthorized;
    private String username;
    private Path userPath;

    public Path getUserPath() {
        return userPath;
    }

    public void setUserPath(String userPathString) {
        this.userPath = Paths.get(userPathString);
    }

    private final IDBMain db;

    public ClientConnection(IDBMain db){
        this.isAuthorized = false;
        this.db = db;
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
}
