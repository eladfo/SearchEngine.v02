package View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import Model.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller_View extends Component
{

    public  javafx.scene.control.TextField txtfld_corpus_path ;
    public  javafx.scene.control.TextField txtfld_posting_path ;
    public  javafx.scene.control.TextField txtfld_path_stopwords;
    public javafx.scene.control.CheckBox steam;

    public static String postingPath ="";
    public static boolean is_steam =false;

    public Controller_View() throws IOException {
    }

    public void setCorpusPath()
    {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Corpus Path");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);
        //
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            txtfld_corpus_path.setText(chooser.getSelectedFile().toString());
        }

    }
    public void setPostingsPath()
    {
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

    public void setStopWordsPath()
    {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("StopWords Path");
        FileNameExtensionFilter fne = new FileNameExtensionFilter("*.txt", "txt");
        chooser.setFileFilter(fne);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_path_stopwords.setText(chooser.getSelectedFile().toString());
        }
    }

    public void startButton() throws IOException
    {
        if(!txtfld_corpus_path.getText().isEmpty() && !txtfld_posting_path.getText().isEmpty())
        {
            Main.google = new SearchEngine(txtfld_corpus_path.getText(), txtfld_posting_path.getText(), steam.isSelected() , txtfld_path_stopwords.getText());
            Main.google.runSearchEngine();
            openDetailsWindow();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter corpus&postings paths");
            alert.showAndWait();
        }
    }

    private void openDetailsWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Show_Details.fxml").openStream());
        Scene scene = new Scene(root, 397, 280);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
        stage.show();
    }

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

    public void resetIndex()
    {
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

    public void loadDics() throws IOException {
        if( postingPath.equals("")|| postingPath.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter posting's path");
            alert.showAndWait();
        }
        else
        {
            Main.google.idx.loadDics(txtfld_posting_path.getText(), steam.isSelected());
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "All dictionaries successfully loaded to memory");
            alert.showAndWait();
        }
    }
}
