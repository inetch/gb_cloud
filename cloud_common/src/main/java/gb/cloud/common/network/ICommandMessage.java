package gb.cloud.common.network;

import org.json.simple.JSONObject;

import java.nio.file.Path;

public interface ICommandMessage {
    String getTargetFolder();
    void setTargetFolder(String targetFolder);
    String getErrorMessage();
    void setErrorMessage(String errorMessage);
    boolean isOk();
    void setResult(boolean result);
    JSONObject getFileTree();
    void setFileTree(JSONObject fileTree);
    long getFileSize();
    void setFileSize(long fileSize);
    Path getFilePath();
    void setFilePath(Path filePath);
    User getUser();
    void setUser(User user);
    Command getCommand();
}
