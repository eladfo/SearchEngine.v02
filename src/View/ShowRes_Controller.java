package View;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparator;


public class ShowRes_Controller extends Component {

    public javafx.scene.control.TextArea  tabel;

    public void initialize()
    {
        String[] arr;
        StringBuilder s = new StringBuilder();
            s.append("Num_query").append("  ").append("Doc_id\n");
            for (String line : Main.google.result_qurey)
            {
                arr = split(line , "~");
                s.append(arr[0]).append("           ").append(arr[1]).append("\n");
            }
            tabel.setText(s.toString());
            tabel.scrollLeftProperty();
            tabel.selectPositionCaret(tabel.getLength());
            tabel.deselect();



    }
}
