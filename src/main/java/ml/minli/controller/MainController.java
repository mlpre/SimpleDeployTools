package ml.minli.controller;

import com.jcraft.jsch.Session;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ml.minli.util.SSHUtil;
import org.ini4j.Wini;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainController {
    @FXML
    public VBox main;
    @FXML
    public MenuItem ssh;
    @FXML
    public MenuItem scp;
    @FXML
    public MenuItem set;
    @FXML
    public MenuItem use;
    @FXML
    public MenuItem about;
    @FXML
    public TextField webPathField;
    @FXML
    public TextField webServerPathField;
    @FXML
    public TextField ipField;
    @FXML
    public TextField portField;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextArea message;

    public static String webPath;

    public static String webServerPath;

    public static String ip;

    public static String port;

    public static String userName;

    public static String passWord;

    public static String webPackage;

    public static String webServerPackage;

    public static String webClean;

    public static String webServerClean;

    public static String shell;

    @FXML
    public JFXButton connect;

    private static Session session;

    public void setIniFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择配置文件");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("配置文件", "*.ini"));
        File file = fileChooser.showOpenDialog(main.getScene().getWindow());
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    if (file != null) {
                        Wini wini = new Wini(file);
                        webPath = wini.get("Path", "webPath", String.class);
                        webServerPath = wini.get("Path", "webServerPath", String.class);
                        ip = wini.get("SSH", "ip", String.class);
                        port = wini.get("SSH", "port", String.class);
                        userName = wini.get("SSH", "userName", String.class);
                        passWord = wini.get("SSH", "passWord", String.class);
                        webPackage = wini.get("Command", "webPackage", String.class);
                        webServerPackage = wini.get("Command", "webServerPackage", String.class);
                        webClean = wini.get("Command", "webClean", String.class);
                        webServerClean = wini.get("Command", "webServerClean", String.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> {
                            webPathField.setText(webPath);
                            webServerPathField.setText(webServerPath);
                            ipField.setText(ip);
                            portField.setText(port);
                            usernameField.setText(userName);
                            passwordField.setText(passWord);
                        });
                        return null;
                    }
                }).start();
            }
        }).start();
    }

    public void useInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("使用说明");
        alert.setResizable(false);
        alert.setContentText("选择前后端项目路径即可执行打包清理操作，连接服务器后即可执行部署相关操作。");
        alert.show();
    }

    public void aboutInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("开发者");
        alert.setResizable(false);
        alert.setContentText("By Minli");
        alert.show();
    }

    public void chooiceWeb() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择前端项目路径");
        directoryChooser.setInitialDirectory(new File("."));
        File file = directoryChooser.showDialog(main.getScene().getWindow());
        if (file != null) {
            webPath = file.getAbsolutePath();
            Platform.runLater(() -> webPathField.setText(file.getAbsolutePath()));
        }
    }

    public void chooiceWebServer() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择后端项目路径");
        directoryChooser.setInitialDirectory(new File("."));
        File file = directoryChooser.showDialog(main.getScene().getWindow());
        if (file != null) {
            webServerPath = file.getAbsolutePath();
            Platform.runLater(() -> webServerPathField.setText(file.getAbsolutePath()));
        }
    }

    public void packageWeb() {
        if (checkString(webPath, webPackage)) {
            configNotFound();
        } else {
            exec(webPath, webPackage);
        }
    }

    public void cleanWeb() {
        if (checkString(webPath, webClean)) {
            configNotFound();
        } else {
            exec(webPath, webClean);
        }
    }

    public void packageWebServer() {
        if (checkString(webServerPath, webServerPackage)) {
            configNotFound();
        } else {
            exec(webServerPath, webServerPackage);
        }
    }

    public void cleanWebServer() {
        if (checkString(webServerPath, webServerClean)) {
            configNotFound();
        } else {
            exec(webServerPath, webServerClean);
        }
    }

    private void exec(String path, String cmd) {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec("cmd /c cd /d " + path + " & " + cmd);
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                String temp = "";
                while ((line = bufferedReader.readLine()) != null) {
                    temp += line + "\r\n";
                    String show = temp;
                    Platform.runLater(() -> {
                        message.setText(show);
                        message.appendText("");
                        message.setScrollTop(Double.MAX_VALUE);
                    });
                }
                return null;
            }
        }).start();
    }

    private void configNotFound() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("配置项未找到");
        alert.setResizable(false);
        alert.setContentText("请检查配置文件是否正确！");
        alert.show();
    }

    private boolean checkString(String... string) {
        for (String str : string) {
            if (str == null || "".equals(str)) {
                return true;
            }
        }
        return false;
    }

    public void sshTools() {
        try {
            Parent ssh = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/ssh.fxml"));
            Scene scene = new Scene(ssh);
            Stage stage = new Stage();
            stage.setTitle("SSH控制台");
            stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/logo.png")));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scpTools() {
        try {
            Parent ssh = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/scp.fxml"));
            Scene scene = new Scene(ssh);
            Stage stage = new Stage();
            stage.setTitle("SCP工具");
            stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/logo.png")));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectServer() {
        SSHUtil.setOut(message);
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if ((session == null || !session.isConnected()) && "连接".equals(connect.getText())) {
                        session = SSHUtil.getJSchSession(usernameField.getText(), passwordField.getText(), ipField.getText(), Integer.valueOf(portField.getText()));
                        System.out.println("连接成功！");
                        Platform.runLater(() -> {
                            ipField.setDisable(true);
                            portField.setDisable(true);
                            usernameField.setDisable(true);
                            passwordField.setDisable(true);
                            connect.setText("断开");
                            connect.setStyle("-fx-background-color: #CC0033");
                        });
                        SSHUtil.execCommand(session, "bash", message);
                    } else if (session != null && session.isConnected() && "断开".equals(connect.getText())) {
                        session.disconnect();
                        System.out.println("连接断开！");
                        Platform.runLater(() -> {
                            ipField.setDisable(false);
                            portField.setDisable(false);
                            usernameField.setDisable(false);
                            passwordField.setDisable(false);
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

    public void oneDeploy() {
        try {
            SSHUtil.execCommand(session, shell, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
