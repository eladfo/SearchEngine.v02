package View;

import javafx.fxml.Initializable;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;


//yiuh
public class Control_Show_Dic extends Component implements Initializable
{
    public javafx.scene.control.TextArea  txt_area;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        File file = new File(Controller.Path_name + "\\FinalTermsDic_NoStemmer");
        String st;
        StringBuilder s = new StringBuilder();
        try {
            BufferedReader  br = new BufferedReader(new FileReader(file));
            while((st = br.readLine()) !=null)
            {
                s.append(st).append("\n");
            }
            txt_area.setText(s.toString());
            txt_area.scrollLeftProperty();
            txt_area.selectPositionCaret(txt_area.getLength());
            txt_area.deselect();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}