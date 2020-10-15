package gb.cloud.common.network;

import java.util.Arrays;
import java.util.Objects;

public class User {
    private String login;
    private byte[] passwordHash;

    public String getLogin() {
        return login;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public User(String login, byte[] passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login.equals(user.login) &&
                Arrays.equals(passwordHash, user.passwordHash);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(login);
        result = 31 * result + Arrays.hashCode(passwordHash);
        return result;
    }
}
