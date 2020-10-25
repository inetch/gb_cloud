package gb.cloud.common.network;

import java.util.Arrays;
import java.util.Objects;

public class User {
    private String login;
    private String passwordHash;

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login.equals(user.login) &&
                passwordHash.equals(passwordHash);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(login);
        result = 31 * result + Objects.hash(passwordHash);
        return result;
    }
}
