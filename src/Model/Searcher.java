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
        try
        {
            semantic_words.clear();
            String[] st = splitByWholeSeparator(content.toString(), "},{");
            String[] st1;
            String[] st2;
            char c;
            for (int i = 0; i < 1 && i < st.length; i++) {
                st1 = splitByWholeSeparator(st[i], ",");
                st2 = splitByWholeSeparator(st1[0], ":");
                c = '"';
                st2[1] = replaceChars(st2[1], c, '*');
                st2[1] = replace(st2[1], "*", "");
                semantic_words.add(st2[1]);
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return semantic_words;
        }
        return semantic_words;
    }

    public HashMap<String, ArrayList<String[]>> createBetaMap(String q, String postingPath) throws IOException {
        betaMap = new HashMap<>();
        ArrayList<String> querie ;
        query = q;
        String[] tokens = split(query, " ");

        if(semanticFlag)
            querie = addSemantic(tokens);
        else
            querie=add_querie(tokens);


        for (String word : querie) {
            String docID;
            String termID;
            int termTF;
            int termDF;
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
                updateBetaMap(docID, termID, termTF, termDF, tmp[tmp.length-1]);
            }
        }
        return betaMap;
    }

    private ArrayList<String> add_querie(String[] tokens)
    {
        ArrayList<String> arr = new ArrayList<>();
        for(String w : tokens)
            arr.add(w);
        return arr;
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

    private ArrayList<String> addSemantic(String[] querie) throws IOException {
        ArrayList<String> semantic = new ArrayList<>();
        ArrayList<String> res = new ArrayList<>();

        for(String s : querie)
        {
                res.add(s);
                semantic = Get_semantica(s);
                for (String semanticWord : semantic)
                {
                    if (index.finalTermsDic.containsKey(upperCase(semanticWord)))
                        res.add(upperCase(semanticWord));
                    else if (index.finalTermsDic.containsKey(lowerCase(semanticWord)))
                        res.add(lowerCase(semanticWord));
                }
        }
        return res;
    }

}
