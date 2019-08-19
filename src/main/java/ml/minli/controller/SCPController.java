package ml.minli.controller;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ml.minli.util.SSHUtil;

import java.io.File;

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
    @FXML
    public JFXButton oneUpload;
    @FXML
    public JFXProgressBar progress;

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

    public void upload() {
        try {
            if (filePath.getText() == null || serverPath.getText() == null || "".equals(filePath.getText()) || "".equals(serverPath.getText())) {
                return;
            }
            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    SSHUtil.upload(session, filePath.getText(), serverPath.getText(), new SftpProgressMonitor() {
                        long count = 0;

                        @Override
                        public void init(int i, String s, String s1, long l) {
                            Platform.runLater(() -> progress.setProgress(0));
                        }

                        @Override
                        public boolean count(long l) {
                            if (count < 99) {
                                count++;
                                Platform.runLater(() -> progress.setProgress(count));
                            }
                            return true;
                        }

                        @Override
                        public void end() {
                            Platform.runLater(() -> progress.setProgress(100));
                        }
                    });
                    return null;
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooiceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择本地文件");
        fileChooser.setInitialDirectory(new File("."));
        File file = fileChooser.showOpenDialog(filePath.getScene().getWindow());
        if (file != null) {
            Platform.runLater(() -> filePath.setText(file.getAbsolutePath()));
        }
    }

    public void chooiceDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择本地文件夹");
        directoryChooser.setInitialDirectory(new File("."));
        File file = directoryChooser.showDialog(filePath.getScene().getWindow());
        if (file != null) {
            Platform.runLater(() -> filePath.setText(file.getAbsolutePath()));
        }
    }
}
