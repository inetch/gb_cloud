package gb.cloud.client.gui;

import gb.cloud.client.ClientSettings;
import gb.cloud.client.network.IResponse;
import gb.cloud.client.network.Network;
import gb.cloud.common.CommonSettings;
import gb.cloud.common.FileTreeElement;
import gb.cloud.common.header.JSONProcessor;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.nio.file.Paths;
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

    private static final JFileChooser fileChooser   = new JFileChooser();

    private static final DefaultMutableTreeNode treeRootNode = new DefaultMutableTreeNode();

    private static final JTree dirTree = new JTree(treeRootNode);
    private User user;

    private String startPath = System.getProperty("user.home");

    AuthDialog authDialog = new AuthDialog();

    @Override
    public void actionPerformed(ActionEvent e) {
        //Object src = e.getSource();
    }

    GBCloudClient(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(ClientSettings.WINDOW_TITLE);
        setSize(WIDTH, HEIGHT);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) ((dimension.getWidth() - WIDTH) / 2), (int) ((dimension.getHeight() - HEIGHT) / 2));

        topPanel.add(loginButton);
        topPanel.add(registerButton);

        rightPanel.add(pullTreeButton);
        rightPanel.add(pushFileButton);
        rightPanel.add(pullFileButton);
        rightPanel.add(createDirectoryButton);

        wrapPanel.add(topPanel, BorderLayout.NORTH);
        wrapPanel.add(rightPanel, BorderLayout.EAST);
        wrapPanel.add(dirTree, BorderLayout.CENTER);
        dirTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        buttonListeners();

        authorized(false);

        add(wrapPanel);

        Connect();

        setVisible(true);
    }

    private void buttonListeners(){
        loginButton.addActionListener( (ActionEvent e) -> {
            for(Component c: topPanel.getComponents()) c.setEnabled(false);
            authDialog.showDialog(true, this);
        });
        registerButton.addActionListener( (ActionEvent e) -> {
            for(Component c: topPanel.getComponents()) c.setEnabled(false);
            authDialog.showDialog(false, this);
        });
        pullTreeButton.addActionListener((ActionEvent e) -> {
            CommandMessage message = new CommandMessage(Command.PULL_TREE);
            Network.simpleSend(message);
        });
        pushFileButton.addActionListener((ActionEvent e ) -> {
            fileChooser.setCurrentDirectory(new File(startPath));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                startPath = fileChooser.getSelectedFile().toPath().getParent().toString();
                CommandMessage message = new CommandMessage(Command.PUSH_FILE);
                message.setFilePath(fileChooser.getSelectedFile().toPath());
                message.setTargetFolder(getSelectedPath(false));
                Network.simpleSend(message);
            }
        });
        pullFileButton.addActionListener((ActionEvent e) -> {
            if(dirTree.getSelectionCount() > 0) {
                if(((FileTreeElement)((DefaultMutableTreeNode)dirTree.getLeadSelectionPath().getLastPathComponent()).getUserObject()).isFolder()){ //OMG
                    showError("Please select a file, not a folder");
                }else{
                    CommandMessage message = new CommandMessage(Command.PULL_FILE);
                    message.setFilePath(Paths.get(getSelectedPath(true)));

                    Network.simpleSend(message);
                }
            }
        });
        createDirectoryButton.addActionListener((ActionEvent e) -> {
            if(dirTree.getSelectionCount() > 0) {
                String name = JOptionPane.showInputDialog(this, "New folder name:");
                if (!name.isEmpty()) {
                    CommandMessage message = new CommandMessage(Command.CREATE_DIR);
                    message.setTargetFolder(getSelectedPath(false) + name);
                    Network.simpleSend(message);
                }
            }
        });
    }

    private String getSelectedPath(boolean withFile){
        StringBuilder sbPath = new StringBuilder();
        if(dirTree.getSelectionCount() > 0){
            TreePath tPath = dirTree.getLeadSelectionPath();
            for(Object o: tPath.getPath()){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)o;
                FileTreeElement el = (FileTreeElement) node.getUserObject();
                if(el.isFolder()){
                    sbPath.append(el.getName());
                    sbPath.append('/');
                }else{
                    if(withFile)sbPath.append(el.getName());
                    break; //impossible to have a child under a file node
                }
            }
        }
        return sbPath.toString();
    }

    private void authorized(boolean ok){
        dirTree.setEnabled(ok);
        rightPanel.setEnabled(ok);
        for(Component c: rightPanel.getComponents()) c.setEnabled(ok);
        for(Component c: topPanel.getComponents()) c.setEnabled(!ok);

        if(ok){
            setTitle(ClientSettings.WINDOW_TITLE + " [" + user.getLogin() + "]");
        }else{
            setTitle(ClientSettings.WINDOW_TITLE);
        }
        repaint();
    }

    public void showError(String error){
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showFileTree(CommandMessage message){
        JSONObject rootFolder = (JSONObject)message.getFileTree().get(CommonSettings.J_FOLDER);
        treeRootNode.removeAllChildren();
        treeRootNode.setUserObject(new FileTreeElement((String)rootFolder.get(CommonSettings.J_FILENAME), true));
        if(rootFolder.containsKey(CommonSettings.J_LIST)){
            JSONArray list = (JSONArray) rootFolder.get(CommonSettings.J_LIST);
            JSONProcessor.parseJSONTree(list, treeRootNode);
            DefaultTreeModel model = (DefaultTreeModel)dirTree.getModel();
            model.reload();
            dirTree.expandPath(new TreePath(treeRootNode.getPath()));
        }
    }

    @Override
    public void gotOk(CommandMessage message){
        if(message.getCommand() == Command.LOGIN || message.getCommand() == Command.REGISTER){
            user = message.getUser();
            JOptionPane.showMessageDialog(this, "Welcome, " + user.getLogin(), "Welcome", JOptionPane.INFORMATION_MESSAGE);
            authorized(message.isOk());
        }
        if(message.getFileTree() != null){
            showFileTree(message);
        }
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
        showError(e.getMessage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GBCloudClient());
    }

}
