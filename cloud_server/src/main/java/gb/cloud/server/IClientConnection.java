package gb.cloud.server;

import gb.cloud.common.network.User;

import java.io.IOException;
import java.nio.file.Path;

public interface IClientConnection {
    Path getUserPath();
    void setUserPath(String userPathString);
    boolean login(User user) throws IOException;
    boolean register(User user) throws IOException;
    boolean isAuthorized();
}
