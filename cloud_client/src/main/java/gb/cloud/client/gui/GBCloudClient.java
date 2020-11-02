package gb.cloud.client.gui;

import gb.cloud.client.ClientSettings;
import gb.cloud.client.network.IResponse;
import gb.cloud.client.network.Network;
import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.Sender;
import gb.cloud.common.network.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class GBCloudClient extends JFrame implements ActionListener, IResponse, Thread.UncaughtExceptionHandler {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private static final JPanel wrapPanel = new JPanel(new BorderLayout());
    private static final JPanel topPanel = new JPanel(new FlowLayout());
    private static final JPanel rightPanel = new JPanel(new GridLayout(4, 1));

    private static final JButton loginButton = new JButton("Login");
    private static final JButton registerButton = new JButton("Register");

    private static final JButton pullTreeButton = new JButton("Refresh");
    private static final JButton pushFileButton = new JButton("Push file");
    private static final JButton pullFileButton = new JButton("Pull file");
    private static final JButton createDirectoryButton = new JButton("New dir");

    private static DefaultMutableTreeNode treeRootNode = new DefaultMutableTreeNode();

    private static final JTree dirTree = new JTree(treeRootNode);
    private User user;

    AuthDialog authDialog = new AuthDialog();

    @Override
    public void actionPerformed(ActionEvent e) {
        //Object src = e.getSource();
    }

    class FileTreeElement {
        private final String name;
        private final boolean isFolder;
        private final long size;

        private static final double KB = 1024;
        private static final double MB = KB * 1024;
        private static final double GB = MB * 1024;
        private static final double TB = GB * 1024;

        public FileTreeElement(String name, boolean isFolder, long size){
            this.name = name;
            this.isFolder = isFolder;
            this.size = size;
        }

        public FileTreeElement(String name, boolean isFolder){
            this.name = name;
            this.isFolder = isFolder;
            this.size = 0;
        }

        private String getSizeString(long size){
            if(size > TB){
                return String.format("%.2f TB", (double)size / TB);
            }
            if(size > GB){
                return String.format("%.2f GB", (double)size / GB);
            }
            if(size > MB){
                return String.format("%.2f MB", (double)size / MB);
            }
            if(size > KB){
                return String.format("%.2f kB", (double)size / KB);
            }
            return Long.toString(size);
        }

        @Override
        public String toString() {
            if(isFolder){
                return "(D) " + name;
            }else{
                return "(f) " + name + " [" + getSizeString(size) + "]";
            }
        }
    }

    GBCloudClient(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(ClientSettings.WINDOW_TITLE);
        setSize(WIDTH, HEIGHT);

        topPanel.add(loginButton);
        topPanel.add(registerButton);

        rightPanel.add(pullTreeButton);
        rightPanel.add(pushFileButton);
        rightPanel.add(pullFileButton);
        rightPanel.add(createDirectoryButton);

        wrapPanel.add(topPanel, BorderLayout.NORTH);
        wrapPanel.add(rightPanel, BorderLayout.EAST);
        wrapPanel.add(dirTree, BorderLayout.CENTER);

        loginButton.addActionListener( (ActionEvent e) ->  authDialog.showDialog(true, this));
        registerButton.addActionListener( (ActionEvent e) ->  authDialog.showDialog(false, this));
        pullTreeButton.addActionListener((ActionEvent e) -> {
            CommandMessage message = new CommandMessage(Command.PULL_TREE);
            try {
                Sender.sendMessage(message, false, Network.getInstance().getCurrentChannel().pipeline().firstContext(), null);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        authorized(false);

        add(wrapPanel);

     /*   JSONObject header = new JSONObject();
        header.put(CommonSettings.J_COMMAND, Command.PULL_TREE.toString());
        Sender.sendHeader(header, Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }else{
                System.out.println("Pull file");
            }
        });*/

        Connect();

        setVisible(true);
    }

    private void authorized(boolean ok){
        System.out.println("authorized");
        dirTree.setEnabled(ok);
        rightPanel.setEnabled(ok);
        for(Component c: rightPanel.getComponents()){
            c.setEnabled(ok);
        }
        for(Component c: topPanel.getComponents()){
            c.setEnabled(!ok);
        }
        if(ok){
            setTitle(ClientSettings.WINDOW_TITLE + " [" + user.getLogin() + "]");
        }else{
            setTitle(ClientSettings.WINDOW_TITLE);
        }
        repaint();
    }

    private void parseJSONTree(JSONArray list, DefaultMutableTreeNode node){
        if(list == null) return;
        list.stream()
                .sorted((o1, o2) -> {
                    JSONObject json1 = (JSONObject)o1;
                    JSONObject json2 = (JSONObject)o2;
                    if(json1.containsKey(CommonSettings.J_FOLDER)){
                        if(json2.containsKey(CommonSettings.J_FILE)){
                            return -1;
                        }else if(json2.containsKey(CommonSettings.J_FOLDER)){
                            String s1 = (String)((JSONObject)json1.get(CommonSettings.J_FOLDER)).get(CommonSettings.J_FILENAME);
                            String s2 = (String)((JSONObject)json2.get(CommonSettings.J_FOLDER)).get(CommonSettings.J_FILENAME);
                            return s1.compareTo(s2);
                        }else{
                            return 0;
                        }
                    }else if (json1.containsKey(CommonSettings.J_FILE)){
                        if(json2.containsKey(CommonSettings.J_FOLDER)){
                            return 1;
                        }else if(json2.containsKey(CommonSettings.J_FILE)){
                            String s1 = (String)((JSONObject)json1.get(CommonSettings.J_FILE)).get(CommonSettings.J_FILENAME);
                            String s2 = (String)((JSONObject)json2.get(CommonSettings.J_FILE)).get(CommonSettings.J_FILENAME);
                            return s1.compareTo(s2);
                        }else{
                            return 0;
                        }
                    }
                    return 0;
                })
                .forEach(o -> {
                    JSONObject json = (JSONObject)o;
                    if(json.containsKey(CommonSettings.J_FILE)){
                        JSONObject file = (JSONObject) json.get(CommonSettings.J_FILE);
                        node.add(new DefaultMutableTreeNode(new FileTreeElement((String)file.get(CommonSettings.J_FILENAME), false, (long)file.get(CommonSettings.J_SIZE))));
                    }else if(json.containsKey(CommonSettings.J_FOLDER)){
                        JSONObject file = (JSONObject) json.get(CommonSettings.J_FOLDER);
                        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(new FileTreeElement((String)file.get(CommonSettings.J_FILENAME), true));
                        parseJSONTree((JSONArray) file.get(CommonSettings.J_LIST), folderNode);
                        node.add(folderNode);
                    }else{
                        System.out.println("WTF?: " + json);
                    }
                });
    }

    public void showError(String error){
        System.out.println(error);
    }

    @Override
    public void gotOk(CommandMessage message){
        System.out.println("OK: " + message.getCommand().toString());
        if(message.getCommand() == Command.LOGIN || message.getCommand() == Command.REGISTER){
            user = message.getUser();
            authorized(message.isOk());
        }
        if(message.getCommand() == Command.PUSH_TREE){
            JSONObject rootFolder = (JSONObject)message.getFileTree().get(CommonSettings.J_FOLDER);
            String folderName = (String)rootFolder.get(CommonSettings.J_FILENAME);
            //DefaultMutableTreeNode root
            treeRootNode.setUserObject(new FileTreeElement((String)rootFolder.get(CommonSettings.J_FILENAME), true));
            if(rootFolder.containsKey(CommonSettings.J_LIST)){
                JSONArray list = (JSONArray) rootFolder.get(CommonSettings.J_LIST);
                parseJSONTree(list, treeRootNode);
                dirTree.repaint();
            }
        }
    }

    @Override
    public void gotError(CommandMessage message){
        System.out.println("ERROR: " + message.getCommand().toString());
        authorized(message.isOk());
        showError(message.getErrorMessage());
    }

    @Override
    public void networkError(){
        showError("Network error!");
    }

     /*   Sender.sendFile(header, Paths.get("hw2.png"), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }else{
                System.out.println("Header sent!");
            }
            Network.getInstance().stop();
        });*/

        /*
         JSONObject fileEntry = new JSONObject();
        fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
        fileEntry.put(CommonSettings.J_SIZE, fileSize);
        header.put(CommonSettings.J_FILE, fileEntry);
        * */

        /*
        header.put(CommonSettings.J_COMMAND, Command.PULL_FILE.toString());

        JSONObject fileEntry = new JSONObject();
        fileEntry.put(CommonSettings.J_FILENAME, "send.me");
        header.put(CommonSettings.J_FILE, fileEntry);

        Sender.sendHeader(header, Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }else{
                System.out.println("Pull file");
            }
        });*/



     /*   header.put(CommonSettings.J_COMMAND, Command.LOGIN.toString());
        header.put(CommonSettings.J_USERNAME, "user-figuser");
        header.put(CommonSettings.J_PASSWORD, "password-figasword");

        Sender.sendHeader(header, Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }else{
                System.out.println("Header sent!");
            }
            Network.getInstance().stop();
        });*/

        /*obj.put("name", "foo");
        obj.put("num", new Integer(100));
        obj.put("balance", new Double(1000.21));
        obj.put("is_vip", new Boolean(true));*/


//        ProtoFileSender.sendFile(Paths.get("demo.txt"), Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
////                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
////                Network.getInstance().stop();
//            }
//        });
////        Thread.sleep(2000);
//        ProtoFileSender.sendFile(Paths.get("demo1.txt"), Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
////                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
////                Network.getInstance().stop();
//            }
//        });

    private void Connect(){
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter, this)).start();

        try {
            networkStarter.await();
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
      /*  StackTraceElement[] ste = e.getStackTrace();
        String msg = String.format("Exception in \"%s\": %s %s%n\t %s",
                t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        JOptionPane.showMessageDialog(this, msg, "Exception!", JOptionPane.ERROR_MESSAGE);*/
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GBCloudClient());
    }

}
