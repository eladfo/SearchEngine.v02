package View;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.split;


public class ShowQueryRes_Controller extends Component {

    public javafx.scene.control.TextArea results_txtArea;
    public Button saveResults_Button;
    public Button searchEnt_Button;
    public TextField docID_txtfld;
    public TextArea entities_txtArea;

    public void initialize()
    {
        String[] arr;
        StringBuilder s = new StringBuilder();
            s.append("Num_query").append("          ").append("Doc_id\n");
            for (String line : Main.google.result_qurey)
            {
                arr = split(line , "~");
                s.append(arr[0]).append("                       ").append(arr[1]).append("\n");
            }
            results_txtArea.setText(s.toString());
            results_txtArea.scrollLeftProperty();
            results_txtArea.selectPositionCaret(results_txtArea.getLength());
            results_txtArea.deselect();
    }

    public void saveResButton() throws IOException {
        Main.google.saveSearchResults();
        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Results successfully saved");
        alert.showAndWait();
    }

    public void showEntitiesButton() throws IOException {
        if (!docID_txtfld.getText().isEmpty()) {
            String[] res = Main.google.getEntitiesPerDoc(docID_txtfld.getText(), MainWindow_Controller.isStemm);
            if(res == null){
                Alert alert = new Alert(Alert.AlertType.ERROR,"Document does not exist!");
                alert.showAndWait();
                return;
            }
            if(res[0].equals(" ")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Document have no entities");
                alert.showAndWait();
            }
            StringBuilder s = new StringBuilder();
            for (String line : res)
            {
                s.append(line).append("\n");
            }
            entities_txtArea.setText(s.toString());
            entities_txtArea.scrollLeftProperty();
            entities_txtArea.selectPositionCaret(entities_txtArea.getLength());
            entities_txtArea.deselect();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Please enter docID");
            alert.showAndWait();
        }
    }
}
