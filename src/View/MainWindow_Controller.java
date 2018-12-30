package View;

import Model.SearchEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.split;

public class MainWindow_Controller extends Component {
    public TextField txtfld_corpus_path;
    public TextField txtfld_posting_path;
    public TextField txtfld_stopwords_path;
    public TextField txtfld_queriesFile_path;
    public TextField txtfld_singleQuery;
    public CheckBox steam;
    public RadioButton semanticFlag;
    public RadioButton stemmFlag;
    public CheckComboBox<String> cityFilter;
    public Button createInvertedIdx;
    public Button showDic;
    public Button loadDic;
    public Button resetIdx;
    public Button browseQueryFile;
    public Button runQueryFile;
    public Button runSingleQuery;

    public boolean loadedDics = false;
    public static String postingPath = "";
    public static boolean is_steam = false;

    /**
     * Open File chooser to chose the path of Corpus files.
     */
    public void setCorpusPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Corpus Path");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_corpus_path.setText(chooser.getSelectedFile().toString());
        }
        createInvertedIdxCheck();
    }

    /**
     * Open File chooser to chose the path that will save all posting files.
     */
    public void setPostingsPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Posting Path");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_posting_path.setText(chooser.getSelectedFile().toString());
            postingPath = txtfld_posting_path.getText();
        }
        createInvertedIdxCheck();
    }

    /**
     * Open File chooser to chose the path of stop word txt file.
     */
    public void setStopWordsPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("StopWords Path");
        FileNameExtensionFilter fne = new FileNameExtensionFilter("*.txt", "txt");
        chooser.setFileFilter(fne);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_stopwords_path.setText(chooser.getSelectedFile().toString());
        }
        createInvertedIdxCheck();
    }

    /**
     * Start the createInvertedIdx inverted files process.
     */
    public void startButton() throws IOException {
            Main.google = new SearchEngine(txtfld_corpus_path.getText(), txtfld_posting_path.getText()
                    , steam.isSelected(), txtfld_stopwords_path.getText());
            Main.google.runSearchEngine();
            openDetailsWindow();
            loadedDics = true;
            enableBottuns();
            String updatePath = updatePostingPath(steam.isSelected());
            Main.google.index.loadDics(updatePath);
    }

    public void createInvertedIdxCheck() {
        if (!txtfld_corpus_path.getText().isEmpty()
                && !txtfld_posting_path.getText().isEmpty()
                    && !txtfld_stopwords_path.getText().isEmpty()) {
            createInvertedIdx.setDisable(false);
        }
    }

    /**
     * Open a stage that showing info about inverted files process (the stage show in the end of the process).
     */
    private void openDetailsWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("SummaryDetails.fxml").openStream());
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
        if (postingPath.equals("") || postingPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please choose posting's path");
            alert.showAndWait();
        } else {
            is_steam = steam.isSelected();
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
     * Resetting all data structures in project and delete all inverted files that createInvertedIdx in the program.
     */
    public void totalIndexReset() {
        if (Main.google != null) {
            if (postingPath != "") {
                Main.google.resetIdx();
                disableButtons();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Index successfully reset");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please choose posting's path");
                alert.showAndWait();
            }
        }
    }

    /**
     * Load the terms dictionary to java memory.
     */
    public void loadDicsToMemory() throws IOException {
        if (postingPath.equals("") || postingPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please choose posting's path");
            alert.showAndWait();
        } else {
            String updatePath = updatePostingPath(steam.isSelected());
            Main.google.setPostingsPath(postingPath);
            Boolean res = Main.google.index.loadDics(updatePath);
            if(res) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "All dictionaries successfully loaded to memory");
                alert.showAndWait();
                loadedDics = true;
                enableBottuns();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Dictionaries loaded failed. Maybe one of them is missing.");
                alert.showAndWait();
            }
        }
    }

    private String updatePostingPath(Boolean flag) {
        if(flag)
            return postingPath + "\\With_Stemmer";
        else
            return postingPath + "\\Without_Stemmer";
    }

    public void setQueriesFilePath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("QueriesFile Path");
        FileNameExtensionFilter fne = new FileNameExtensionFilter("*.txt", "txt");
        chooser.setFileFilter(fne);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_queriesFile_path.setText(chooser.getSelectedFile().toString());
        }
    }

    public void runQueriesButton() throws IOException {
        if (!txtfld_queriesFile_path.getText().isEmpty() && loadedDics) {
            ArrayList<String> cityList = getCitiesSelected();
            String path = updatePostingPath(stemmFlag.isSelected());
            Main.google.partB(txtfld_queriesFile_path.getText(), path, stemmFlag.isSelected(), semanticFlag.isSelected(), cityList);
            Show_res();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Please choose queries's file path");
            alert.showAndWait();
        }
    }

    private void Show_res() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Show_res.fxml").openStream());
        Scene scene = new Scene(root,330 , 370);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
        stage.show();
    }

    public void runSingle() throws IOException {
        if (!txtfld_singleQuery.getText().isEmpty() && loadedDics) {
            ArrayList<String> cityList = getCitiesSelected();
            String path = updatePostingPath(stemmFlag.isSelected());
            Main.google.runSingleQuery(txtfld_queriesFile_path.getText(), path, stemmFlag.isSelected(), semanticFlag.isSelected(), cityList);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Please enter a query");
            alert.showAndWait();
        }
    }

    public void initializeCityFilter() throws IOException {
        ObservableList<String> strings = FXCollections.observableArrayList();
        String upPath = updatePostingPath(steam.isSelected());
        File file = new File(upPath + "\\Final_Cities_Dic");
        String st;
        String[] arr_str;
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((st = br.readLine()) != null) {
            arr_str = split(st, ";");
            strings.add(arr_str[0]);
        }
        br.close();
        cityFilter.getItems().addAll(strings);
        cityFilter.setDisable(false);
    }

    public void enableBottuns() throws IOException {
        showDic.setDisable(false);
        runQueryFile.setDisable(false);
        runSingleQuery.setDisable(false);
        browseQueryFile.setDisable(false);
        resetIdx.setDisable(false);
        initializeCityFilter();
    }

    public void disableButtons(){
        showDic.setDisable(true);
        runQueryFile.setDisable(true);
        runSingleQuery.setDisable(true);
        browseQueryFile.setDisable(true);
        cityFilter.setDisable(true);
    }

    public ArrayList<String> getCitiesSelected(){
        ArrayList<String> res = new ArrayList<>();
        ObservableList<Integer> idxCheackedCities = cityFilter.getCheckModel().getCheckedIndices();
        for (Integer i : idxCheackedCities) {
            res.add((String) cityFilter.getItems().get(i));
        }
        return res;
    }
}
