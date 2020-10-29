package gb.cloud.client.gui;

import gb.cloud.client.network.IResponse;
import gb.cloud.client.network.Network;
import gb.cloud.common.network.CommandMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

public class GBCloudClient extends JFrame implements ActionListener, IResponse {
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

    private static final JTree dirTree = new JTree();

    AuthDialog authDialog = new AuthDialog();

    @Override
    public void actionPerformed(ActionEvent e) {
        //Object src = e.getSource();
    }

    GBCloudClient(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("GBCloud File Client");
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
        dirTree.setEnabled(ok);
        rightPanel.setEnabled(ok);
    }

    public void showError(String error){
        System.out.println(error);
    }

    @Override
    public void gotOk(CommandMessage message){
        authorized(message.isOk());
    }

    @Override
    public void gotError(CommandMessage message){
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GBCloudClient());
    }

}
