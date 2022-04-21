package io.github.mlpre.ui;

import io.github.mlpre.util.LanguageUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import io.github.mlpre.util.FileUtil;

import java.util.Optional;

public class MainUI extends Application {

    public void start(Stage stage) throws Exception {
        Parent main = FXMLLoader.load(FileUtil.getResource("fxml/main.fxml"), LanguageUtil.resourceBundle);
        stage.setTitle(LanguageUtil.getValue("app.title"));
        Scene scene = new Scene(main);
        stage.getIcons().add(new Image(FileUtil.getResourceAsStream("img/logo.png")));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(LanguageUtil.getValue("app.exit"));
            alert.setContentText(LanguageUtil.getValue("app.isExit"));
            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Platform.exit();
                    System.exit(0);
                } else {
                    event.consume();
                }
            });
        });
        stage.show();
    }

}
