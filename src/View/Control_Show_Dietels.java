package View;

import javafx.fxml.Initializable;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Control_Show_Dietels extends Component implements Initializable
{
    public  javafx.scene.control.Label Num_term;
    public  javafx.scene.control.Label Num_doc ;
    public  javafx.scene.control.Label total_rt ;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Num_term.setText(Controller.serch.get_num_term());
        Num_doc.setText(Controller.serch.get_num_doc());
        total_rt.setText(Controller.serch.get_num_rt());
    }

}
