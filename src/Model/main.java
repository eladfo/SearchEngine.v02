package Model;

import java.io.*;

public class main {
    public static void main(String[] args) throws IOException
    {
        SearchEngine searcher = new SearchEngine("C:\\Users\\e-pc\\IdeaProjects\\corpus\\corpus",
                                                   "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\Postingsss",
                                                    false);
        searcher.createSearchEngine();
    }
}

