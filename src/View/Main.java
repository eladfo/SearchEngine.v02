package View;

import Model.SearchEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

import static javafx.application.Platform.exit;

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
        String a1 = "C:\\Users\\A\\Downloads\\corpus\\corpus";
        String a2 = "C:\\Users\\A\\Downloads\\Searcher2\\Searcher\\pos_with_header\\Without_Stemmer";
        String a3 = "C:\\Users\\A\\Downloads\\corpus\\stop_words.txt";
        String a4 = "C:\\Users\\A\\Downloads\\Searcher2\\Searcher\\queries.txt";
//
//        String e1 = "C:\\Users\\e-pc\\IdeaProjects\\corpus\\corpus";
//        String e2 = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\posting";
//        String e3 = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\resources\\stop_words.txt";
//        String e4 = "C:\\Users\\A\\Downloads\\Searcher2\\Searcher\\queries.txt";
        /*
        try {
            SearchEngine google = new SearchEngine(a1, a2, false, a3);
            //google.runSearchEngine();
//            google.testing(e2);
            google.index.loadDics(a2);
            google.partB(a4, a2, false,false , new ArrayList<>());
           exit();
       } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }


}
