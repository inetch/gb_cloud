package gb.cloud.common.network;

import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.Objects;

public class CommandMessage extends AbstractMessage {
    private final Command command;
    private User user;
    private Path filePath;
    private long fileSize;
    private boolean result;
    private String errorMessage;
    private JSONObject fileTree;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isOk() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public JSONObject getFileTree() {
        return fileTree;
    }

    public void setFileTree(JSONObject fileTree) {
        this.fileTree = fileTree;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public CommandMessage(Command command){
        this.result = true;
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
