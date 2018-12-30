package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static View.MainWindow_Controller.postingPath;
import static java.lang.Character.isUpperCase;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private String query;
    private ArrayList<String> cityFilter;
    private boolean semanticFlag;
    private ArrayList<Term> queryTerms;
    private Indexer index;
    public ArrayList<String> semantic_words ;
    public ArrayList<String> word;
    public HashMap<String, ArrayList<String[]>> betaMap;
    public String postPath;

    public Searcher(ArrayList<String> cityF, boolean semanticF, Indexer idx, String postPath) {
        queryTerms = new ArrayList<>();
        cityFilter = cityF;
        semanticFlag = semanticF;
        index = idx;
        semantic_words= new ArrayList<>();
        word = new ArrayList<>();
        this.postPath = postPath;
    }

    public void createTermsList(String q, String postingPath) throws IOException {
        queryTerms = new ArrayList<>();
        query = q;

        //if we want to run query with semantic:
       //   q = add_semantic_word(q);


        String[] tokens = split(query, " ");
        for (String word : tokens) {
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
                continue;
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
            t.Name =word;
            queryTerms.add(t);
        }
    }

    private String add_semantic_word(String q) throws IOException {
        String[] arr = split(q," ");
        StringBuilder sb = new StringBuilder(q);
        for(String s : arr)
        {
            for(String semantic_word : Get_semantica(s))
            {
                sb.append(semantic_word).append(" ");
            }
        }
        return sb.toString();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<Term> getQueryTerms() {
        return queryTerms;
    }

    public ArrayList<String>  Get_semantica (String word) throws IOException {
        ArrayList<String> res;
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
        res = parse_semantica(content);
        in.close();
        con.disconnect();
        return res;
    }

    private ArrayList<String> parse_semantica(StringBuffer content)
    {
        String[] st = splitByWholeSeparator(content.toString(),"},{");
        String[] st1 ;
        String[] st2;
        char c;
        for(int i=0 ; i<2 && i<st.length;i++)
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

    /**
     * ***** Beta Map *****
     * ***** Beta Map *****
     * ***** Beta Map *****
     */


    public HashMap<String, ArrayList<String[]>> createBetaMap(String q, String postingPath) throws IOException {
        betaMap = new HashMap<>();
        query = q;
        String[] tokens = split(query, " ");
        for (String word : tokens) {
            System.out.println(word);
            String docID;
            String termID;
            int termTF;
            int termDF;
            String headerFlag;
            if(index.finalTermsDic.containsKey(upperCase(word))) {
                termID = upperCase(word);
            }else {
                termID = lowerCase(word);
            }
            int[] termDicData = index.finalTermsDic.get(termID);
            if(termDicData == null)
                continue;
            termDF = termDicData[0];
            int termRowPtr = termDicData[2] + 1;
            String termData = termData(termID, termRowPtr);
            if(termData.isEmpty())
                continue;
            String[] docs = split(termData, "~");
            for(String d : docs) {
                String[] tmp = split(d, ",");
                termTF = tmp.length - 1;
                docID = tmp[0];
                updateBetaMap(docID, termID,termTF, termDF, tmp[tmp.length-1]);
            }
        }
        return betaMap;
    }

    private void updateBetaMap(String docID, String termID, int termTF, int termDF, String headerFlag) {
        if(!headerFlag.equals("0"))
            headerFlag = "1";
        if(!betaMap.containsKey(docID)){
            ArrayList<String[]> betaDocData = new ArrayList<>();
            betaDocData.add(new String[]{termID, String.valueOf(termTF), String.valueOf(termDF), headerFlag});
            betaMap.put(docID, betaDocData);
        }
        else {
            ArrayList<String[]> tmp = betaMap.get(docID);
            tmp.add(new String[]{termID, String.valueOf(termTF), String.valueOf(termDF), String.valueOf(0), headerFlag});
        }
    }

    public String termData(String termID, int termRowPtr) throws IOException
    {
        System.out.println(postPath);
        File termPost = new File
                (postPath + "\\" + (upperCase(Character.toString(termID.charAt(0)))));
        if(!termPost.exists())
            return "";
        BufferedReader brTermPost = new BufferedReader(new FileReader(termPost));
        for(int i=0; i<termRowPtr-1; i++)
            brTermPost.readLine();
        return brTermPost.readLine();
    }
}
