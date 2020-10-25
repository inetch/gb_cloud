package gb.cloud.server;

public class Authorizer {
    public static boolean auth(String username, String password){
        System.out.println("authorize username: " + username + "; password: " + password);
        return true;
    }

    public static boolean register(String username, String password){
        System.out.println("register username: " + username + "; password: " + password);
        return true;
    }
}
