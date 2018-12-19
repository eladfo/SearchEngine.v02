package Model;

import java.io.*;
import java.util.ArrayList;
import static java.lang.Character.isUpperCase;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private String query;
    private boolean cityFlag;
    private boolean semanticFlag;
    private ArrayList<Term> queryTerms;
    private Indexer index;

    public Searcher(boolean cityF, boolean semanticF, Indexer idx) {
        queryTerms = new ArrayList<>();
        cityFlag = cityF;
        semanticFlag = semanticF;
        index = idx;
    }

    public void createTermsList(String q, String postingPath) throws IOException {
        queryTerms = new ArrayList<>();
        query = q;
        String[] tokens = split(query, ";");
        for (String word : tokens) {
            Term t;
            if(isUpperCase(word.charAt(0))) {
                t = new Term(null, null, 1);
                upperCase(word);
            }else
                t = new Term(null, null,0);
            int[] test = index.finalTermsDic.get(word);
            if(test == null)
            {
                queryTerms.add(t);
                continue;
            }
            int termRowPtr = test[2] + 1;
            BufferedReader brTermPost = new BufferedReader(new FileReader(new File
                                (postingPath + "\\" + (upperCase(Character.toString(word.charAt(0)))))));
            for(int i=0; i<termRowPtr-1; i++)
                brTermPost.readLine();
            String termData = brTermPost.readLine();
            String[] docs = split(termData, "~");
            for(String d : docs) {
                String[] tmp = split(d, ",");
                int tf = tmp.length - 1;
                int docLength = index.finalDocsDic.get(tmp[0])[0];
                t.addDoc(tmp[0], new StringBuilder(tf+";"+docLength));
            }
            queryTerms.add(t);
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<Term> getQueryTerms() {
        return queryTerms;
    }
}
