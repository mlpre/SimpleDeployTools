import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimpleDeployToolsApp extends Application {
    public void start(Stage primaryStage) throws Exception {
        Parent main = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));
        primaryStage.setTitle("简单部署工具(By Minli)");
        primaryStage.setResizable(false);
        Scene scene = new Scene(main, 800, 600);
        scene.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
