package View;

import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import Model.*;
public class Controller extends Component
{

    public javafx.scene.control.TextField txtfld_corpus_path;
    public javafx.scene.control.TextField txtfld_posting_path;
    public javafx.scene.control.CheckBox steam;

    public SearchEngine serch ;

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
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_corpus_path.setText(chooser.getSelectedFile().toString());
        }

    }
    public void Browse_posting_path()
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
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtfld_posting_path.setText(chooser.getSelectedFile().toString());
        }

    }

    public void Start() throws IOException
    {
        serch = new SearchEngine(txtfld_corpus_path.getText(), txtfld_posting_path.getText(), steam.isSelected() );
        serch.createSearchEngine();
    }
}
