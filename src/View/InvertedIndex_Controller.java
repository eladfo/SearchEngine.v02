package View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import Model.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InvertedIndex_Controller extends Component
{
    public  javafx.scene.control.TextField txtfld_corpus_path ;
    public  javafx.scene.control.TextField txtfld_posting_path ;
    public  javafx.scene.control.TextField txtfld_stopwords_path;
    public javafx.scene.control.CheckBox steam;
    public static String postingPath ="";
    public static boolean is_steam =false;

    /**
     * Controller of primaryStage.
     */
    public InvertedIndex_Controller() {}

    /**
     * Open File chooser to chose the path of Corpus files.
     */
    public void setCorpusPath() {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Corpus Path");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        //
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            txtfld_corpus_path.setText(chooser.getSelectedFile().toString());
        }

    }

    /**
     * Open File chooser to chose the path that will save all posting files.
     */
    public void setPostingsPath() {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Posting Path");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_posting_path.setText(chooser.getSelectedFile().toString());
            postingPath = txtfld_posting_path.getText();
        }
    }

    /**
     * Open File chooser to chose the path of stop word txt file.
     */
    public void setStopWordsPath() {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("StopWords Path");
        FileNameExtensionFilter fne = new FileNameExtensionFilter("*.txt", "txt");
        chooser.setFileFilter(fne);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_stopwords_path.setText(chooser.getSelectedFile().toString());
        }
    }

    /**
     * Start the create inverted files process.
     */
    public void startButton() throws IOException {
        if(!txtfld_corpus_path.getText().isEmpty() && !txtfld_posting_path.getText().isEmpty())
        {
            Main.google = new SearchEngine(txtfld_corpus_path.getText(), txtfld_posting_path.getText()
                                                , steam.isSelected() , txtfld_stopwords_path.getText());
            Main.google.runSearchEngine();
            openDetailsWindow();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter corpus&postings paths");
            alert.showAndWait();
        }
    }

    /**
     * Open a stage that showing info about inverted files process (the stage show in the end of the process).
     */
    private void openDetailsWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("SummaryDetails_Controller.fxml").openStream());
        Scene scene = new Scene(root, 397, 280);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
        stage.show();
    }

    /**
     * Open a stage that showing terms dictionary info.
     */
    public void showDicButton() throws IOException {
        if( postingPath.equals("")|| postingPath.isEmpty()  )
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter posting's path");
            alert.showAndWait();
        }
        else
        {
            is_steam = steam.isSelected();
            postingPath = txtfld_posting_path.getText();
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Show_Dic.fxml").openStream());
            Scene scene = new Scene(root, 613, 651);
            scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        }
    }

    /**
     * Resetting all data structures in project and delete all inverted files that create in the program.
     */
    public void totalIndexReset() {
        if(Main.google != null){
            if(postingPath != "") {
                Main.google.Reset(postingPath);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Index successfully reset");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter posting's path");
                alert.showAndWait();
            }
        }
    }

    /**
     * Load the terms dictionary to java memory.
     */
    public void loadDicsToMemory() throws IOException {
        if(postingPath.equals("")|| postingPath.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter posting's path");
            alert.showAndWait();
        }
        else
        {
            Main.google.index.loadDics(txtfld_posting_path.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "All dictionaries successfully loaded to memory");
            alert.showAndWait();
        }
    }
}
