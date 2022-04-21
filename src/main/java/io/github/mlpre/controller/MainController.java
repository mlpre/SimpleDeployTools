package io.github.mlpre.controller;

import com.google.common.base.Ascii;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jfoenix.controls.JFXButton;
import io.github.mlpre.util.LanguageUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import io.github.mlpre.util.CustomSettingsProvider;
import io.github.mlpre.util.JSchShellTtyConnector;
import io.github.mlpre.util.SSHUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController {

    @FXML
    public VBox root;
    @FXML
    public TextField ip;
    @FXML
    public TextField userName;
    @FXML
    public PasswordField passWord;
    @FXML
    public JFXButton connect;
    @FXML
    public StackPane container;

    public static JediTermWidget message;

    public AtomicBoolean ctrl = new AtomicBoolean(false);

    public AtomicBoolean c = new AtomicBoolean(false);

    private AtomicBoolean sshStatus = new AtomicBoolean(false);

    private Thread thread = new Thread(new Task<Void>() {
        @Override
        protected Void call() {
            while (true) {
                if (sshStatus.get() && ctrl.get() && c.get()) {
                    try {
                        byte[] bytes = new byte[]{Ascii.ETX};
                        message.getTtyConnector().write(bytes);
                        if (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    public synchronized void connectServer() {
        if (ip.getText() == null || ip.getText().isEmpty()) {
            alertWarningMessage(LanguageUtil.getValue("app.ip.null"));
            return;
        }
        if (userName.getText() == null || userName.getText().isEmpty()) {
            alertWarningMessage(LanguageUtil.getValue("app.username.null"));
            return;
        }
        if (passWord.getText() == null || passWord.getText().isEmpty()) {
            alertWarningMessage(LanguageUtil.getValue("app.password.null"));
            return;
        }
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if (LanguageUtil.getValue("app.connect").equals(connect.getText())) {
                        String realIp = ip.getText();
                        int realPort = 22;
                        if (ip.getText().contains(":")) {
                            String[] string = realIp.split(":");
                            realIp = string[0];
                            realPort = Integer.parseInt(string[1]);
                        }
                        SSHUtil.initSSHParam(realIp, realPort, userName.getText(), passWord.getText());
                        Platform.runLater(() -> {
                            ip.setDisable(true);
                            userName.setDisable(true);
                            passWord.setDisable(true);
                            connect.setText(LanguageUtil.getValue("app.disconnect"));
                            connect.setStyle("-fx-background-color: #CC0033");
                            SwingNode swingNode = new SwingNode();
                            createAndSetSwingContent(swingNode);
                            container.getChildren().add(swingNode);
                        });
                        if (!thread.isAlive()) {
                            thread.start();
                        }
                        sshStatus.set(true);
                        root.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.CONTROL) {
                                ctrl.set(true);
                            }
                            if (event.getCode() == KeyCode.C) {
                                c.set(true);
                            }
                        });
                        root.setOnKeyReleased(event -> {
                            if (event.getCode() == KeyCode.CONTROL) {
                                ctrl.set(false);
                            }
                            if (event.getCode() == KeyCode.C) {
                                c.set(false);
                            }
                        });
                    } else if (LanguageUtil.getValue("app.disconnect").equals(connect.getText())) {
                        sshStatus.set(false);
                        Platform.runLater(() -> {
                            ip.setDisable(false);
                            userName.setDisable(false);
                            passWord.setDisable(false);
                            connect.setText(LanguageUtil.getValue("app.connect"));
                            connect.setStyle("-fx-background-color: #009966");
                            container.getChildren().removeAll(container.getChildren());
                            message.close();
                        });
                    }
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

    private void createAndSetSwingContent(SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            message = createTerminalWidget();
            swingNode.setContent(message);
        });
    }

    private JediTermWidget createTerminalWidget() {
        JediTermWidget jediTermWidget = new JediTermWidget(80, 15, new CustomSettingsProvider());
        jediTermWidget.setTtyConnector(new JSchShellTtyConnector());
        jediTermWidget.start();
        return jediTermWidget;
    }

    private void alertWarningMessage(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(LanguageUtil.getValue("app.warning"));
        alert.setResizable(false);
        alert.setContentText(text);
        alert.show();
    }

    public synchronized void oneDeploy() throws Exception {
        if (!sshStatus.get()) {
            alertWarningMessage(LanguageUtil.getValue("app.ssh.disconnect"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageUtil.getValue("app.shell.choice"));
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(LanguageUtil.getValue("app.shell"), "*.sh"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null || !file.exists()) {
            return;
        }
        JSchShellTtyConnector jSchShellTtyConnector = (JSchShellTtyConnector) message.getTtyConnector();
        SSHUtil.upload(
                jSchShellTtyConnector.getMySession(),
                new FileInputStream(file),
                "/root", file.getName());
        jSchShellTtyConnector.write(("chmod +x /root " + file.getName() + "\r").getBytes(StandardCharsets.UTF_8));
        jSchShellTtyConnector.write(("bash /root/" + file.getName() + "\r").getBytes(StandardCharsets.UTF_8));
    }

}
