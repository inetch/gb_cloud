package gb.cloud.server.db;

import gb.cloud.common.network.User;
import gb.cloud.common.password.Password;
import gb.cloud.server.ServerSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DBSQLite implements IDBMain{
    private Connection connection;
    private final Logger logger = LogManager.getLogger(DBSQLite.class);

    private String className;
    private String connectionString;

    private boolean isConnected = false;

    private static final String stmtUnsuccessfulRegisterLog = "insert into usr_log_vw (action_rowid, login, hash) values (" + ServerSettings.DB_REGISTER_UNSUCCESSFUL + ", ?, ?)";
    private static final String stmtUnsuccessfulLoginLog = "insert into usr_log_vw (action_rowid, login, hash) values (" + ServerSettings.DB_LOGIN_UNSUCCESSFUL + ", ?, ?)";
    private static final String stmtSuccessfulLoginLog = "insert into usr_log_vw (action_rowid, login, hash) values (" + ServerSettings.DB_LOGIN + ", ?, ?)";

    private static final String stmtGetUserHash = "select hash from usr_user_vw where login = ?";
    private static final String stmtCreateUser = "insert into usr_user_vw (login, hash) values (?, ?)";

    public DBSQLite(String connectionString, String className){
        this.connectionString = connectionString;
        this.className = className;
    }

    public void setConnectionString(String connectionString){
        this.connectionString = connectionString;
        reconnect();
    }

    private void setClassName(String className){
        this.className = className;
        reconnect();
    }

    private void reconnect() {
        if (isConnected) {
            disconnect();
            try {
                connect();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() throws ClassNotFoundException {
        Class.forName(this.className);
        try {
            connection = DriverManager.getConnection(this.connectionString);
            isConnected = true;
        }catch (SQLException sqle){
            logger.fatal(sqle);
        }
    }

    private void disconnect(){
        try {
            isConnected = false;
            connection.close();
        }catch (SQLException throwable){
            throwable.printStackTrace();
        }
    }

    private void userLog(Connection conn, User user, String logStatement){
        PreparedStatement stmt;
        try{
            stmt = conn.prepareStatement(logStatement);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, Password.getHash(user.getPassword()));
            stmt.executeUpdate();
        }catch (SQLException sqle){
            LogManager.getLogger(DBSQLite.class).fatal(sqle);
        }
    }

    public boolean registerUser(User user){
        try {
            connect();
            PreparedStatement stmt;
            try{
                stmt = connection.prepareStatement(stmtCreateUser);
                stmt.setString(1, user.getLogin());
                stmt.setString(2, Password.getHash(user.getPassword()));
            }catch (SQLException sqle){
                logger.fatal(sqle);
                return false;
            }

            try{
                stmt.executeUpdate();
            }catch (SQLException sqle){
                userLog(connection, user, stmtUnsuccessfulRegisterLog);
                logger.error(sqle);
                return false;
            }

        }catch (ClassNotFoundException e){
            logger.fatal(e);
            return false;
        }finally {
            disconnect();
        }
        return true;
    }

    public boolean loginUser(User user){
        try {
            connect();
            PreparedStatement stmt;
            try{
                stmt = connection.prepareStatement(stmtGetUserHash);
                stmt.setString(1, user.getLogin());
                ResultSet res = stmt.executeQuery();

                if(!res.next()){
                    res.close();
                    logger.warn("login: invalid user {}", user.getLogin());
                    return false;
                }

                String dbHash = res.getString(1);

                if(!dbHash.equals(Password.getHash(user.getPassword()))){
                    userLog(connection, user, stmtUnsuccessfulLoginLog);
                    logger.error("login: invalid password for user {}", user.getLogin());
                    return false;
                }else{
                    userLog(connection, user, stmtSuccessfulLoginLog);
                    logger.info("login: user {} logged in", user.getLogin());
                }

            }catch (SQLException sqle){
                logger.fatal(sqle);
                return false;
            }

        }catch (ClassNotFoundException e){
            logger.fatal(e);
            return false;
        }
        return true;
    }
}
