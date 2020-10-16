package gb.cloud.common.network;

import java.io.Serializable;
import java.util.Objects;

public class CommandMessage extends AbstractMessage {
    private Command command;
    private User user;

    public CommandMessage(Command command){
        this.command = command;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Command getCommand(){
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandMessage that = (CommandMessage) o;
        return command == that.command &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, user);
    }
}
