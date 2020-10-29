package gb.cloud.common.network;

import java.util.Objects;

public class User {
    private String login;
    private String password;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public User(String login, String passwordHash) {
        this.login = login;
        this.password = passwordHash;
    }

    public User(){ }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login.equals(user.login) &&
                password.equals(password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(login);
        result = 31 * result + Objects.hash(password);
        return result;
    }
}
