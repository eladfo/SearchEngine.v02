package View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import Model.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller extends Component
{

    public  javafx.scene.control.TextField txtfld_corpus_path ;
    public  javafx.scene.control.TextField txtfld_posting_path ;
    public javafx.scene.control.CheckBox steam;

    public static SearchEngine serch ;
    public static String Path_name="";



    public Controller() throws IOException {
    }


    public void Browse_corpus_path()
    {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("");
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
    public void Browse_posting_path()
    {
        JFileChooser  chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_posting_path.setText(chooser.getSelectedFile().toString());
            Path_name = txtfld_posting_path.getText();
        }
    }

    public void Start() throws IOException
    {
        if(!txtfld_corpus_path.getText().isEmpty() && !txtfld_posting_path.getText().isEmpty())
        {
            serch = new SearchEngine(txtfld_corpus_path.getText(), txtfld_posting_path.getText(), steam.isSelected());
            serch.createSearchEngine();
            open_window_details();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enter All The fields!");
            Optional<ButtonType> result = alert.showAndWait();
        }
    }

    private void open_window_details() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Show_dietels.fxml").openStream());
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
        stage.show();
    }

    public void Show_dic() throws IOException {
        if( Path_name.equals("")|| Path_name.isEmpty()  )
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enter All The fields!");
            Optional<ButtonType> result = alert.showAndWait();
        }
        else
        {
            Path_name = txtfld_posting_path.getText();
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Show_Dictonary.fxml").openStream());
            Scene scene = new Scene(root, 400, 670);
            scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        }



    }


    public void Reset()
    {
        if(serch != null)
            serch.Reset();
    }
}
