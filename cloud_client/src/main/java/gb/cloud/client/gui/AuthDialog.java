package gb.cloud.client.gui;

import gb.cloud.client.network.Network;
import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.Sender;
import gb.cloud.common.network.User;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

public class AuthDialog extends JDialog/* implements ActionListener*/ {

    /*@Override
    public void actionPerformed(ActionEvent e) {
        //Object src = e.getSource();
    }*/

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;

    private final JPanel wrapPanel = new JPanel(new GridLayout(3, 1));

    private final JPanel userPanel = new JPanel(new GridLayout(2, 1));
    private final JPanel passPanel = new JPanel(new GridLayout(2, 1));
    private final JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));

    private final JTextField userText = new JTextField();

    private final JPasswordField passwordText = new JPasswordField();

    private final JButton okButton = new JButton("OK");
    private final JButton cancelButton = new JButton("Cancel");

    private boolean isLogin;

    public void showDialog(boolean isLogin, GBCloudClient mainWindow){
        this.isLogin = isLogin;
        if(isLogin){
            setTitle("Authorization");
        }else{
            setTitle("Registration");
        }

        setVisible(true);
    }

    public AuthDialog() {
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);

        userPanel.add(new JLabel("user:"));
        userPanel.add(userText);
        passPanel.add(new JLabel("password:"));
        passPanel.add(passwordText);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        wrapPanel.add(userPanel);
        wrapPanel.add(passPanel);
        wrapPanel.add(buttonsPanel);

        add(wrapPanel);

        okButton.addActionListener((ActionEvent e) -> {
            Command command;
            if(isLogin){
                command = Command.LOGIN;
            }else{
                command = Command.REGISTER;
            }
            CommandMessage message = new CommandMessage(command);
            message.setUser(new User(userText.getText(), Arrays.toString(passwordText.getPassword())));
            try {
                Sender.sendMessage(message, false, Network.getInstance().getCurrentChannel().pipeline().firstContext(), null);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            setVisible(false);
        });
    }
}
