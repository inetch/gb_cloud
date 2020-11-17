package gb.cloud.server.db;

import gb.cloud.common.network.User;

public interface IDBMain {
    boolean registerUser(User user);
    boolean loginUser(User user);
}
