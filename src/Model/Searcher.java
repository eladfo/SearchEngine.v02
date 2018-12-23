package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import static java.lang.Character.isUpperCase;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private String query;
    private boolean cityFlag;
    private boolean semanticFlag;
    private ArrayList<Term> queryTerms;
    private ArrayList<Term> semanticTerms;
    private Indexer index;
    public ArrayList<String> semantic_words ;
    public ArrayList<String> word;

    public Searcher(boolean cityF, boolean semanticF, Indexer idx) {
        queryTerms = new ArrayList<>();
        cityFlag = cityF;
        semanticFlag = semanticF;
        index = idx;
        semantic_words= new ArrayList<>();
        word = new ArrayList<>();
    }

    public void createTermsList(String q, String postingPath) throws IOException {
        queryTerms = new ArrayList<>();
        query = q;
        String[] tokens = split(query, " ");

        for(int i=0 ; i<tokens.length ; i++)
        {
            word.add(tokens[i]);
            Get_semantica(tokens[i]);
            for(String s : semantic_words)
                word.add(s);
            semantic_words.clear();
        }

        for (String word : word) {
            System.out.println(word);
            Term t;
            if(index.finalTermsDic.containsKey(upperCase(word))) {
                t = new Term(null, null, 1);
                word = upperCase(word);
            }else {
                t = new Term(null, null, 0);
                word = lowerCase(word);
            }


            int[] test = index.finalTermsDic.get(word);
            if(test == null)
            {
                //queryTerms.add(t);
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


    public void  Get_semantica (String word) throws IOException {

        URL url = new URL("https://api.datamuse.com/words?ml="+word);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        InputStreamReader input_stem =  new InputStreamReader(con.getInputStream());
        BufferedReader in = new BufferedReader(input_stem);
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null)
        {
            content.append(inputLine);
        }
        parse_semantica(content);
        in.close();
        con.disconnect();
    }

    private ArrayList<String> parse_semantica(StringBuffer content)
    {
        String[] st = splitByWholeSeparator(content.toString(),"},{");
        String[] st1 ;
        String[] st2;
        char c;
        for(int i=0 ; i<0 && i<st.length;i++)
        {
            st1 = splitByWholeSeparator(st[i],",");
            st2=splitByWholeSeparator(st1[0],":");
            c = '"';
            st2[1] = replaceChars(st2[1],c,'*');
            st2[1] = replace(st2[1],"*","");

            semantic_words.add(st2[1]);
        }
        return semantic_words;
    }
}
