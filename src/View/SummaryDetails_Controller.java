package View;

import javafx.fxml.Initializable;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SummaryDetails_Controller extends Component implements Initializable
{
    public  javafx.scene.control.Label Num_term;
    public  javafx.scene.control.Label Num_doc ;
    public  javafx.scene.control.Label total_rt ;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Num_term.setText(Main.google.getTermsNum());
        Num_doc.setText(Main.google.getDocsNum());
        total_rt.setText(Main.google.getRunningTime());
    }

}
