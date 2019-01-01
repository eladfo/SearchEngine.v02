package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private ArrayList<String> cityFilter;
    private boolean semanticFlag;
    private Indexer index;
    public ArrayList<String> semantic_words ;
    public ArrayList<String> word;
    public HashMap<String, ArrayList<String[]>> betaMap;
    public String postPath;

    public Searcher(ArrayList<String> cityF, boolean semanticF, Indexer idx, String postPath) {
        cityFilter = cityF;
        semanticFlag = semanticF;
        index = idx;
        semantic_words= new ArrayList<>();
        word = new ArrayList<>();
        this.postPath = postPath;
    }

    public HashMap<String, ArrayList<String[]>> createBetaMap(String query, String desciption) throws IOException
    {
        betaMap = new HashMap<>();
        ArrayList<String> querie ;
        ArrayList<String> docCityList = new ArrayList<>();
        if(cityFilter.size() > 0)
            docCityList = createDocsCityList(cityFilter);
        query = query + top_3(desciption);
        System.out.println(query);
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
            String termData = "";
            termData = getTermData(termID, termRowPtr);
            if (termData == null || termData.isEmpty())
                continue;
            String[] docs = split(termData, "~");
            for(String d : docs) {
                String[] tmp = split(d, ",");
                docID = tmp[0];
                if (docCityList.contains(docID) || docCityList.size() == 0) {
                    termTF = tmp.length - 1;
                    updateBetaMap(docID, termID, termTF, termDF, tmp[tmp.length - 1]);
                }
            }
        }
        return betaMap;
    }

    private void updateBetaMap(String docID, String termID, int termTF, int termDF, String headerFlag)
    {
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

    public String getTermData(String termID, int termRowPtr) throws IOException
    {
        File termPost = new File
                (postPath + "\\" + (upperCase(Character.toString(termID.charAt(0)))));
        if(!termPost.exists())
            return "";
        BufferedReader brTermPost = new BufferedReader(new FileReader(termPost));
        for(int i=0; i<termRowPtr-1; i++)
            brTermPost.readLine();
        return brTermPost.readLine();
    }

    private ArrayList<String> createDocsCityList(ArrayList<String> cityFilter) throws IOException
    {
        ArrayList<String> res = new ArrayList<>();
        File cityPost = new File(postPath + "\\mergedCityPosting");
        BufferedReader br = new BufferedReader(new FileReader(cityPost));
        for (String cityID : cityFilter) {
            int cityPtr = index.finalCitiesDic.get(cityID);
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null && idx != cityPtr)
                idx++;
            if(line.charAt(0) == ';')
                br.readLine();
            while ((line = br.readLine()) != null && line.charAt(0) != '*') {
                String[] tokens = split(line, " ");
                for (String s : tokens) {
                    res.add(substringBefore(s, ":"));
                }
            }
        }
        return res;
    }

    private String top_3(String s)
    {
        TreeMap<Double, String> query;
        String newQ = "";
        query = new TreeMap<>();

        String[] tokens = split(s , " ");
        for(String word : tokens)
        {
            if(index.finalTermsDic.containsKey(lowerCase(word)))
                query.put((double)index.finalTermsDic.get(lowerCase(word))[1] , lowerCase(word));
            else if(index.finalTermsDic.containsKey(upperCase(word)))
                query.put((double)index.finalTermsDic.get(lowerCase(word))[1] , upperCase(word));
        }

        for (Map.Entry<Double, String> entry : query.entrySet())
            newQ = newQ + entry.getValue() + " ";

        return newQ;
    }

    public ArrayList<String>  Get_semantica (String urlPath,String word) throws IOException
    {
        ArrayList<String> res;
        URL url = new URL(urlPath);

//        URL ur2 = new URL("https://api.datamuse.com/words?ml="+word);
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

    private ArrayList<String> add_querie(String[] tokens)
    {
        ArrayList<String> arr = new ArrayList<>();
        for(String w : tokens)
            arr.add(w);
        return arr;
    }

    private ArrayList<String> addSemantic(String[] querie) throws IOException {
        ArrayList<String> semantic = new ArrayList<>();
        ArrayList<String> res = new ArrayList<>();

        for(String s : querie)
        {
                res.add(s);
                semantic = Get_semantica("https://api.datamuse.com/words?rel_trg=",s);
                for (String semanticWord : semantic)
                {
                    if (index.finalTermsDic.containsKey(upperCase(semanticWord)))
                        res.add(upperCase(semanticWord));
                    else if (index.finalTermsDic.containsKey(lowerCase(semanticWord)))
                        res.add(lowerCase(semanticWord));
                }
            semantic = Get_semantica("https://api.datamuse.com/words?ml=",s);
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
