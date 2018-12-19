package View;

import Model.SearchEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main //extends Application
{
    /**
     * Create SearchEngine object.
     */
    public static SearchEngine google;

    static {
        try {
            google = new SearchEngine("","", true , "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Open a primaryStage of the program.
     */
//    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        Scene scene = new Scene(root, 650, 450);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        primaryStage.setTitle("\"Epic SearchEngine by Alon.T & Elad.F\"");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main class.
     */
    public static void main(String[] args)
    {
//        launch(args);
        String e1 = "C:\\Users\\e-pc\\IdeaProjects\\corpus\\corpus";
        String e2 = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\postinggg";
        String e3 = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\resources\\stop_words.txt";
        String e4 = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\resources\\queries.txt";
        try {
            SearchEngine google = new SearchEngine(e1, e2, false, e3);
//            google.runSearchEngine();
            google.partB(e4, true, true);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
