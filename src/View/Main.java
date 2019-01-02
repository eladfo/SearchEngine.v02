package View;

import Model.SearchEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    /**
     * Create SearchEngine object.
     */
    public static SearchEngine google;

    static {
        try {
            google = new SearchEngine("", "", false, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open a primaryStage of the program.
     */

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MainWindow.fxml").openStream());
        Scene scene = new Scene(root, 750, 550);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        primaryStage.setTitle("\"Epic SearchEngine by Alon.T & Elad.F\"");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main class.
     */


    public static void main(String[] args) {
        launch(args);
    }
}
