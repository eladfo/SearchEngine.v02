package View;

import javafx.fxml.Initializable;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import static org.apache.commons.lang3.StringUtils.*;


public class ShowDic_Controller extends Component implements Initializable
{
    public javafx.scene.control.TextArea  txt_area;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File file;
        if(!InvertedIndex_Controller.is_steam)
            file = new File(InvertedIndex_Controller.postingPath + "\\Without_Stemmer\\Final_Terms_Dic");
        else
            file = new File(InvertedIndex_Controller.postingPath + "\\With_Stemmer\\Final_Terms_Dic");

        String st;
        String[] arr_str;
        StringBuilder s = new StringBuilder();
        try {
            BufferedReader  br = new BufferedReader(new FileReader(file));
            while((st = br.readLine()) !=null)
            {
                arr_str = splitByWholeSeparator(st,";");
                s.append(arr_str[0]+"    "+arr_str[2]+"\n");
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