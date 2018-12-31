package Model;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.*;


public class SearchEngine {
    ReadFile rf;
    Parse parse;
    Searcher search;
    public Indexer index;
    HashSet<Doc> docs;
    int partiotions;
    int indexedDocs;
    int uniqueTerms;
    double totalRunTime;
    boolean stemmFlag;
    String postingsPath;
    Ranker ranker;
    public ArrayList<String> result_qurey;

    /**
     * Constructor. Initialize 3 paths for corpus, posting and stopwords.
     * Calculate the partition amount, by dividing the corpus size by 50.
     */
    public SearchEngine(String corpusPath, String postPath, Boolean isStemm, String stopwordsPath) throws IOException
    {
        rf = new ReadFile(corpusPath);
        this.postingsPath = updatePath(postPath, isStemm);
        partiotions = (int) Math.ceil(rf.getListOfFilesSize()/50.0);
        //partiotions=15;
        stemmFlag = isStemm;
        index = new Indexer(postingsPath, partiotions);
        parse = new Parse(corpusPath, stemmFlag, stopwordsPath);
        docs = new HashSet<>();
        ranker = new Ranker();

    }

    public void setPostingsPath(String postingsPath) {
        this.postingsPath = postingsPath;
    }

    /**
     * Core function that control the whole program,
     * and navigating the data from the disk into ReadFile obj,
     * to the parser and finally to the indexer.
     * For each partition of 50 files, created a tmp posting file.
     */
    public void runSearchEngine() throws IOException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < partiotions; i++) {
            long p0 = System.currentTimeMillis();
            docs.clear();
            docs = rf.readLines(i);
            indexedDocs += docs.size();
            for (Doc d : docs) {
                ParsedDoc pd = parse.runParser(d , stemmFlag);
                index.addParsedDoc(pd);
                d.getDocText().setLength(0);
            }
            index.createTmpPosting(i);
            index.resetIndex();
            long p1 = System.currentTimeMillis();
            System.out.println(i + " "+ (p1 - p0));
        }
        index.createInvertedIndex();
        long endTime = System.currentTimeMillis();
        totalRunTime = (endTime - startTime) / 1000;
        uniqueTerms = index.finalTermsDic.size();
    }

    public String getTermsNum() {
        return String.valueOf(uniqueTerms);
    }

    public String getDocsNum() {
        return String.valueOf(indexedDocs);
    }

    public String getRunningTime() {
        return String.valueOf(totalRunTime);
    }

    public void resetIdx() {
        rf.resetDocSet();
        parse.resetParse();
        index.resetIndex();
        deleteAllFiles(postingsPath);
        docs.clear();
    }

    private void deleteAllFiles(String path){
        String s1 = path + "\\With_Stemmer";
        String s2 = path + "\\Without_Stemmer";
        File d1 = new File(s1);
        File d2 = new File(s2);
        if(d1.exists()){
            if(d1.listFiles() != null) {
                for (File file : d1.listFiles()) {
                    file.delete();
                }
            }
        }
        if(d2.exists()){
            if(d2.listFiles() != null) {
                for (File file : d2.listFiles()) {
                    file.delete();
                }
            }
        }
    }

    public void partB(String qPath, String postingPath, boolean stemmFlag, boolean semanticFlag, ArrayList<String> cityFilter) throws IOException {
        for(String s : cityFilter)
        {
            System.out.println(s);
        }
        System.out.println("================================================================");
        search = new Searcher(cityFilter, semanticFlag, index, postingPath);
        ArrayList<StringBuilder> queries = rfBeta(qPath ,stemmFlag );
        if(semanticFlag)
            queries = addSemantic(queries);
        String[] arr;
        for (StringBuilder entry : queries)
        {
            arr = split(entry.toString(),"~");
            HashMap<String, ArrayList<String[]>> docsMap = search.createBetaMap(arr[1], postingPath);
            String postPath = updatePath(postingPath, stemmFlag);
            result_qurey= ranker.rankerStart(postPath, arr[0], search.getQueryTerms(), index , docsMap);
        }
        saveSearchResults();
    }

    private ArrayList<StringBuilder> addSemantic(ArrayList<StringBuilder> queries) throws IOException {
        ArrayList<String> semantic = new ArrayList<>();
        String[] arr , arr_word;
        for(StringBuilder sb : queries)
        {
            arr = split(sb.toString(),"~");
            arr_word = split(arr[1]," ");
            for(String s : arr_word)
            {
                semantic = search.Get_semantica(s);
                for (String semanticWord : semantic) {
                    if (index.finalTermsDic.containsKey(upperCase(semanticWord)))
                        sb.append(upperCase(semanticWord)).append(" ");
                    else if (index.finalTermsDic.containsKey(lowerCase(semanticWord)))
                        sb.append(lowerCase(semanticWord)).append(" ");
                }
            }
        }
        return queries;
    }


    public void runSingleQuery(String query, String postingPath, boolean stemmFlag, boolean semanticFlag, ArrayList<String> cityFlag) throws IOException {
        search = new Searcher(cityFlag, semanticFlag, index, postingPath);
        String postPath = updatePath(postingPath, stemmFlag);
        HashMap<String, ArrayList<String[]>> docsMap = search.createBetaMap(query, postingPath);
        ranker.rankerStart(postPath ,"007" ,search.getQueryTerms(), index, docsMap);
    }

    public ArrayList<StringBuilder> rfBeta(String qPath,boolean stemmFlag) throws IOException {   //function that Readfile Query file!!!
        ArrayList<StringBuilder> res = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(new File(qPath)));
        String line;
        String[] st;
        String num_query ="";
        while((line = br.readLine()) != null){
            if(contains(line, "<num>"))
            {
                st = split(line , " ");
                num_query = st[st.length-1];
            }
            if(contains(line, "<title>"))
            {
                Doc d = new Doc();
                d.update(substring(line, 8), 4);
                System.out.println(stemmFlag);
                ParsedDoc pd = parse.runParser(d,stemmFlag);
                StringBuilder sb = new StringBuilder();
                sb.append(num_query).append("~");
                for(Map.Entry<String, StringBuilder> entry : pd.getTerms().entrySet())
                    sb.append(entry.getKey()).append(" ");
                res.add(sb);
            }
        }
        return res;
    }

    public void saveSearchResults() throws IOException {
        ranker.Save_res(postingsPath);
    }

    public String[] getEntitiesPerDoc(String docID, boolean isStemm) throws IOException {
        int[] tmp = index.finalDocsDic.get(docID);
        String postPath = updatePath(postingsPath, isStemm);
        File file = new File(postPath + "\\mergedDocsPosting");
        if(!file.exists() || tmp==null)
            return null;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        try{
        for (int j = 0; j < tmp[1] ; j++) {
            line = br.readLine();
        }} catch (java.lang.NullPointerException e){
            System.out.println("SEMEK");        }
        String[] tokens = split(line, "~");
        return split(tokens[5], ",");
    }

    public String updatePath(String path, boolean isStemm){
        if(isStemm)
            return path + "\\With_Stemmer";
        else
            return path + "\\Without_Stemmer";
    }

    public void testing(String e2) throws IOException {
        e2 += "\\Without_Stemmer";
        index.loadDics(e2);
        search = new Searcher(null, false, index, e2);
        String query = "mutual fund predictors";
        HashMap<String, ArrayList<String[]>> test = search.createBetaMap(query, e2);
        System.out.println("well well");
    }
}
