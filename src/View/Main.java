package View;

import Model.SearchEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static javafx.application.Platform.exit;

public class Main  extends Application {
    /**
     * Create SearchEngine object.
     */
    public static SearchEngine google;

    static {
        try {
            google = new SearchEngine("", "", true, "");
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
        //launch(args);
        String e1 = "C:\\Users\\A\\Downloads\\corpus\\corpus";
        String e2 = "C:\\Users\\A\\Downloads\\Searcher\\Searcher\\pos_with_header";
        String e3 = "C:\\Users\\A\\Downloads\\corpus\\stop_words.txt";
        String e4 = "C:\\Users\\A\\Downloads\\Searcher2\\Searcher\\queries.txt";
        try {
            SearchEngine google = new SearchEngine(e1, e2, false, e3);
           google.runSearchEngine();
           //google.index.loadDics(e2);
           //google.partB(e4, true, true);
           exit();
       } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
