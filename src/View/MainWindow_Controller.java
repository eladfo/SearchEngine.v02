package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import java.io.*;
import static org.apache.commons.lang3.StringUtils.split;



public class MainWindow_Controller {
    public CheckComboBox<String> extras;

    public void indexButton() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("InvertedIndex.fxml").openStream());
        Scene scene = new Scene(root, 750, 550);
        scene.getStylesheets().add(getClass().getResource("/ViewStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
        stage.show();
    }

    public void initialize()  {

        ObservableList<String> strings = FXCollections.observableArrayList();
        File file;
        file = new File( "C:\\Users\\A\\Downloads\\Searcher\\Searcher\\pos\\Without_Stemmer\\Final_Cities_Dic");

        String st;
        String[] arr_str;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((st = br.readLine()) !=null)
            {
                arr_str = split(st,";");
                strings.add(arr_str[0]);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        extras.getItems().addAll(strings);
    }
}
