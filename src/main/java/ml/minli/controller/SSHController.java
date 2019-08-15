package ml.minli.controller;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Properties;

public class SSHController {
    @FXML
    public JFXButton connect;
    @FXML
    public TextArea console;
    @FXML
    public TextField exec;
    @FXML
    public TextField ip;
    @FXML
    public TextField userName;
    @FXML
    public PasswordField passWord;

    private static Session session;

    public void connectServer() {
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(userName.getText(), ip.getText(), 22);
            session.setPassword(passWord.getText());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            Platform.runLater(() -> {
                if ("连接".equals(connect.getText())) {
                    connect.setText("断开");
                    connect.setStyle("-fx-background-color: #CC0033");
                } else if ("断开".equals(connect.getText())) {
                    connect.setText("连接");
                    connect.setStyle("-fx-background-color: #009966");
                }
            });
            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    session.connect();
                    System.out.println("连接成功！");
                    Channel channel = session.openChannel("exec");
                    ((ChannelExec) channel).setCommand(exec.getText());
                    channel.setInputStream(System.in);
                    ((ChannelExec) channel).setErrStream(System.err);
                    channel.connect();
                    channel.disconnect();
                    return null;
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execCommand(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    return null;
                }
            }).start();
        }
    }
}
