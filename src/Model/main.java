package Model;

import java.io.*;
import static org.apache.commons.lang3.StringUtils.*;
public class main {
    public static void main(String[] args) throws IOException
    {
        SearchEngine searcher = new SearchEngine("C:\\Users\\e-pc\\IdeaProjects\\corpus\\corpus",
                                                   "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\Postingsss",
                                                    false);
        searcher.runSearchEngine();

    }
}

