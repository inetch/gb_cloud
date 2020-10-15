package gb.cloud.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainWindow extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Box Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        /*primaryStage.setTitle("Hello fucking world");

        Label label1 = new Label("Label1");
        Scene scene = new Scene(label1, 400, 200);
        primaryStage.setScene(scene);

        primaryStage.show();*/
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
