package ml.minli;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ml.minli.controller.MainController;
import org.ini4j.Wini;

public class SimpleDeployToolsApp extends Application {

    private static boolean load = false;

    static {
        try {
            Wini wini = new Wini(SimpleDeployToolsApp.class.getClassLoader().getResource("ini/config.ini"));
            if (!wini.isEmpty()) {
                MainController.webPath = wini.get("Path", "webPath", String.class);
                MainController.webServerPath = wini.get("Path", "webServerPath", String.class);
                MainController.ip = wini.get("SSH", "ip", String.class);
                MainController.port = wini.get("SSH", "port", String.class);
                MainController.userName = wini.get("SSH", "userName", String.class);
                MainController.passWord = wini.get("SSH", "passWord", String.class);
                MainController.webPackage = wini.get("Command", "webPackage", String.class);
                MainController.webServerPackage = wini.get("Command", "webServerPackage", String.class);
                MainController.webClean = wini.get("Command", "webClean", String.class);
                MainController.webServerClean = wini.get("Command", "webServerClean", String.class);
                load = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(Stage primaryStage) throws Exception {
        Parent main = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main.fxml"));
        primaryStage.setTitle("简单部署工具(By Minli)");
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/logo.png")));
        Scene scene = new Scene(main);
        primaryStage.setScene(scene);
        if (load) {
            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        ((TextField) main.lookup("#webPathField")).setText(MainController.webPath);
                        ((TextField) main.lookup("#webServerPathField")).setText(MainController.webServerPath);
                        ((TextField) main.lookup("#ipField")).setText(MainController.ip);
                        ((TextField) main.lookup("#portField")).setText(MainController.port);
                        ((TextField) main.lookup("#usernameField")).setText(MainController.userName);
                        ((TextField) main.lookup("#passwordField")).setText(MainController.passWord);
                    });
                    return null;
                }
            }).start();
        }
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
