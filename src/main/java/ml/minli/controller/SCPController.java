package ml.minli.controller;

import com.jcraft.jsch.Session;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ml.minli.util.SSHUtil;

public class SCPController {
    @FXML
    public TextField ip;
    @FXML
    public TextField userName;
    @FXML
    public PasswordField passWord;
    @FXML
    public JFXButton connect;
    @FXML
    public TextField serverPath;
    @FXML
    public TextField filePath;

    private static Session session;

    public void connectServer() {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if ((session == null || !session.isConnected()) && "连接".equals(connect.getText())) {
                        session = SSHUtil.getJSchSession(userName.getText(), passWord.getText(), ip.getText(), 22);
                        Platform.runLater(() -> {
                            ip.setDisable(true);
                            userName.setDisable(true);
                            passWord.setDisable(true);
                            connect.setText("断开");
                            connect.setStyle("-fx-background-color: #CC0033");
                        });
                    } else if (session != null && session.isConnected() && "断开".equals(connect.getText())) {
                        session.disconnect();
                        Platform.runLater(() -> {
                            ip.setDisable(false);
                            userName.setDisable(false);
                            passWord.setDisable(false);
                            connect.setText("连接");
                            connect.setStyle("-fx-background-color: #009966");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }
}
