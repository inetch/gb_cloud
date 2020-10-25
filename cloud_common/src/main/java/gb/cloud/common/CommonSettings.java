package gb.cloud.common;

public class CommonSettings {
    public static final int SERVER_PORT = 8189;
    public static final int MAX_NETWORK_OBJECT_SIZE = 50 * 1024 * 1024;

    //JSON field names
    public static final String J_COMMAND = "command";
    public static final String J_USERNAME = "username";
    public static final String J_PASSWORD = "password";

    //JSON file entry field names
    public static final String J_FILE = "file";
    public static final String J_FILENAME = "filename";
    public static final String J_SIZE = "size";
    public static final String J_FOLDER = "folder";

    //commands
    public static final String C_AUTH = "auth";
    public static final String C_REG = "register";
    public static final String C_FILE = "file";
    public static final String C_TREE_REQ = "tree-request";
    public static final String C_FILE_REQ = "file-request";

}
