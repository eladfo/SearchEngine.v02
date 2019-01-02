package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.lang.Character.isUpperCase;
import static org.apache.commons.lang3.StringUtils.*;

public class Searcher {

    private ArrayList<String> cityFilter;
    private Indexer index;
    private Parse parser;
    private boolean semanticFlag;
    public ArrayList<String> semantic_words ;
    public ArrayList<String> word;
    public HashMap<String, ArrayList<String[]>> betaMap;
    public String postPath;
    public String originQuery;

    /**
     * Searcher constructor
     */
    public Searcher(ArrayList<String> cityF, boolean semanticF, Indexer idx, Parse p, String postPath) {
        cityFilter = cityF;
        semanticFlag = semanticF;
        index = idx;
        semantic_words= new ArrayList<>();
        word = new ArrayList<>();
        parser = p;
        this.postPath = postPath;
    }

    /**
     * Main function that manage and start everything the searcher does.
     * It creating a HashMap which later passed to the Ranker.
     * Considering description's query, stemming and semantic flags, and also city filter selected by the user(optional).
     * @param query - original query words
     * @param description - query's description words (after parsing)
     * @param stemmFlag
     * @return HashMap containing => KEY= DodID, VALUE= ArrayList of String[termID, TF, DF, header flag]
     * @throws IOException
     */
    public HashMap<String, ArrayList<String[]>> createQueryDataMap(String query, String description, boolean stemmFlag) throws IOException
    {
        originQuery = query;
        betaMap = new HashMap<>();
        ArrayList<String> termsToRank;
        ArrayList<String> docCityList = new ArrayList<>();
        if(cityFilter.size() > 0)
            docCityList = createDocsCityList(cityFilter);
        query = query + top3DescTermsTF(description);
        String[] tokens = split(query, " ");
        if(semanticFlag)
            termsToRank = addSemantic(tokens, originQuery);
        else
            termsToRank = convertQueryArrayToArrayList(tokens);
        termsToRank = stemmQueryTerms(termsToRank, stemmFlag);
        for (String word : termsToRank) {
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
                    updateQueryDataMap(docID, termID, termTF, termDF, tmp[tmp.length - 1]);
                }
            }
        }
        return betaMap;
    }

    /**
     * Func get termID and a pointer to the posting file where the term data store.
     * Open a buffer reader, and reach to the posting file according to the term's first letter.
     * @param termID
     * @param termRowPtr
     * @return Term's data from the posting file.
     * @throws IOException
     */
    private String getTermData(String termID, int termRowPtr) throws IOException
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

    /**
     * Adds doc and term's data(NOTE: Term appear in DOC) to the main HashMap QueryDataMap.
     * If doc is new to the map, adds it as new key, plus the term's data as the first array list item.
     * If doc already exists, link the term's data do the ArrayList of the doc.
     * @param docID
     * @param termID
     * @param termTF
     * @param termDF
     * @param headerFlag - only if equal to 0, term belongs to the doc's header, else not.
     */
    private void updateQueryDataMap(String docID, String termID, int termTF, int termDF, String headerFlag)
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

    /**
     * Get a cities list selected by the user, and create a list containing ALL the docsIDs which contain one of the cities.
     * Gathering the docsIDs, by reading from the merged city posting.
     * @param cityFilter - string list which contain cities selected to filter by the user.
     * @return
     * @throws IOException
     */
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

    private ArrayList<String> convertQueryArrayToArrayList(String[] tokens)
    {
        ArrayList<String> arr = new ArrayList<>();
        for(String w : tokens)
            arr.add(w);
        return arr;
    }

    /**
     * Stemming the final words list by using the parser stemmer.
     * @param termsToRank - words list.
     * @param stemmFlag - True for stemm, false for not.
     * @return
     */
    private ArrayList<String> stemmQueryTerms(ArrayList<String> termsToRank, boolean stemmFlag){
        if (!stemmFlag) {
            return termsToRank;
        } else {
            ArrayList<String> res = new ArrayList<>();
            int upFlag = 0;
            for (String s : termsToRank) {
                if (isUpperCase(s.charAt(0)))
                    upFlag = 1;
                String tmp = parser.stemmer.stem(lowerCase(s));
                if (upFlag == 1)
                    tmp = upperCase(tmp);
                res.add(tmp);
            }
            return res;
        }
    }

    /**
     * Calculating the top 3 words with the most TTF value.
     * @param s - description words.
     * @return - if s.length equal or less then 3, return s.
     *              else return top 3.
     */
    private String top3DescTermsTF(String s)
    {
        TreeMap<Double, String> query;
        String newQ = "";
        query = new TreeMap<>();
        String[] tokens = split(s , " ");
        double i = 8;
        for(String word : tokens)
        {
            newQ = newQ + " " + word;
            query.put((double)i , lowerCase(word));
            i++;
        }
        return newQ;
    }

    /**
     * For only words which appear in query's title, getting words from DataMuse API.
     * @param queryPlusDesc - array containing query's title and description words.
     * @param queryTit - only query's title words.
     * @return ArrayList which contain query + description + semantic words
     * @throws IOException
     */
    private ArrayList<String> addSemantic(String[] queryPlusDesc, String queryTit) throws IOException {
        ArrayList<String> semantic = new ArrayList<>();
        ArrayList<String> res = new ArrayList<>();
        String[] tokens = split(queryTit, " ");
        ArrayList<String> queryTitle = new ArrayList<>(Arrays.asList(tokens));
        for(String s : queryPlusDesc)
        {
            res.add(s);
            if(queryTitle.contains(s)) {
                semantic = getSemanticWords("https://api.datamuse.com/words?rel_trg=", s, 0);
                res.addAll(semantic);
            }
            if(queryTitle.contains(s)) {
                semantic = getSemanticWords("https://api.datamuse.com/words?ml=", s, 2);
                res.addAll(semantic);
            }
        }
        return res;
    }

    /**
     * Connects to DataMuse API, requesting list of synonymy words for "word".
     * @param urlPath - API link.
     * @param word - word to received similar words from the API.
     * @param apiCounter - counter pass to parse_semantic function.
     * @return Words received from the API.
     * @throws IOException
     */
    public ArrayList<String> getSemanticWords(String urlPath, String word, int apiCounter) throws IOException
    {
        ArrayList<String> res;
        URL url = new URL(urlPath + word);
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
        res = parse_semantic(content, apiCounter);
        in.close();
        con.disconnect();
        return res;
    }

    /**
     * Parsing the data list received for particular word from the API,
     * saving only apiCounter number of them.
     * @param content - list of words received from the API.
     * @param apiCounter - num of words to save from content list.
     * @return ArrayList containing "apiCounter" num of words.
     */
    private ArrayList<String> parse_semantic(StringBuffer content, int apiCounter)
    {
        try
        {
            semantic_words.clear();
            String[] st = splitByWholeSeparator(content.toString(), "},{");
            String[] st1;
            String[] st2;
            char c;
            for (int i = 0; i < apiCounter && i < st.length; i++) {
                st1 = splitByWholeSeparator(st[i], ",");
                st2 = splitByWholeSeparator(st1[0], ":");
                c = '"';
                st2[1] = replaceChars(st2[1], c, '*');
                st2[1] = replace(st2[1], "*", "");
                semantic_words.add(parser.stemmer.stem(st2[1]));
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return semantic_words;
        }
        return semantic_words;
    }

}
