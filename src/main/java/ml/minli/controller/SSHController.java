package ml.minli.controller;

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
import ml.minli.util.SSHUtil;

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
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if ((session == null || !session.isConnected()) && "连接".equals(connect.getText())) {
                        session = SSHUtil.getJSchSession(userName.getText(), passWord.getText(), ip.getText(), 22);
                        System.out.println("连接成功！");
                        Platform.runLater(() -> {
                            ip.setDisable(true);
                            userName.setDisable(true);
                            passWord.setDisable(true);
                            connect.setText("断开");
                            connect.setStyle("-fx-background-color: #CC0033");
                            exec.setDisable(false);
                        });
                        SSHUtil.execCommand(session, "ls", console);
                    } else if (session != null && session.isConnected() && "断开".equals(connect.getText())) {
                        session.disconnect();
                        System.out.println("连接断开！");
                        Platform.runLater(() -> {
                            ip.setDisable(false);
                            userName.setDisable(false);
                            passWord.setDisable(false);
                            connect.setText("连接");
                            connect.setStyle("-fx-background-color: #009966");
                            exec.setDisable(true);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

    public void execCommand(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            new Thread(new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        Platform.runLater(() -> exec.setText(null));
                        SSHUtil.execCommand(session, exec.getText(), console);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).start();
        }
    }
}
