package gb.cloud.server;

public class ServerSettings {
    public static final String FILE_DIRECTORY = "server_files/";
    public static final String DB_FILE = "gb-cloud.db";

    //db actions
    public static final int DB_REGISTER = 1;
    public static final int DB_REGISTER_UNSUCCESSFUL = 10;

    public static final int DB_LOGIN = 2;
    public static final int DB_LOGIN_UNSUCCESSFUL = 20;

    public static final int DB_CHANGE_PASS = 3;

    public static final int DB_PUSH_FILE = 4;
    public static final int DB_PULL_FILE = 5;
    public static final int DB_PULL_TREE = 6;

}
