package Model;
import com.sun.javafx.geom.TransformedShape;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.util.*;

import static java.lang.Character.isUpperCase;
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
        //partiotions = (int) Math.ceil(rf.getListOfFilesSize()/50.0);
        partiotions=6;
        stemmFlag = isStemm;
        index = new Indexer(postingsPath, partiotions);
        parse = new Parse(stemmFlag, stopwordsPath);
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

        search = new Searcher(cityFilter, semanticFlag, index, postingPath);
        ArrayList<StringBuilder> queries = rfBeta(qPath ,stemmFlag );
        String[] arr;
        for (StringBuilder entry : queries)
        {
            arr = split(entry.toString(),"~");
            HashMap<String, ArrayList<String[]>> docsMap = search.createBetaMap(arr[1], arr[2], postingPath);
            String postPath = updatePath(postingPath, stemmFlag);
            result_qurey= ranker.rankerStart(postPath, arr[0], search.getQueryTerms(), index , docsMap);
        }
    }

    public void runSingleQuery(String query, String postingPath, boolean stemmFlag, boolean semanticFlag, ArrayList<String> cityFlag) throws IOException
    {
        search = new Searcher(cityFlag, semanticFlag, index, postingPath);
        String postPath = updatePath(postingPath, stemmFlag);
        HashMap<String, ArrayList<String[]>> docsMap = search.createBetaMap(query, null, postingPath);
        ranker.rankerStart(postPath ,"007" ,search.getQueryTerms(), index, docsMap);
    }

    public ArrayList<StringBuilder> rfBeta(String qPath,boolean stemmFlag) throws IOException {   //function that Readfile Query file!!!
        ArrayList<StringBuilder> res = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(new File(qPath)));
        String line;
        String[] st;
        String num_query ="";
        StringBuilder sb = new StringBuilder();
        ArrayList<String> tmpQuery = new ArrayList<>();
        ArrayList<String> trash = createTrashList();
        while((line = br.readLine()) != null){
            if(contains(line, "<num>"))
            {
                sb = new StringBuilder();
                tmpQuery = new ArrayList<>();
                st = split(line , " ");
                num_query = st[st.length-1];
                sb.append(num_query).append("~");
            }
            if(contains(line, "<title>"))
            {
                Doc d = new Doc();
                d.update(substring(line, 8), 4);
                ParsedDoc pd = parse.runParser(d,stemmFlag);
                for(Map.Entry<String, StringBuilder> entry : pd.getTerms().entrySet()) {
                    sb.append(entry.getKey()).append(" ");
                    tmpQuery.add(entry.getKey());
                }
                sb.append("~");
            }
            if(contains(line, "<desc>")){
                while((line = br.readLine()) != null && !contains(line, "<narr>")){
                    StringBuilder desc = parseDesc(line, tmpQuery, trash);
                    if(desc != null)
                        sb.append(desc).append(" ");
                }
                res.add(sb);
            }
        }
        System.out.println(res.toString());
        return res;
    }

    private StringBuilder parseDesc(String line, ArrayList<String> tmpQuery, ArrayList<String> trash) {
        StringBuilder res = new StringBuilder();
        HashSet<String> stopWords = parse.stopWords;
        char x = '"';
        String [] tokens = split(line, x + " `'_*&#+/<>|~\\,;][:^@.()?{}!�");
        for(String s : tokens){
            if(isUpperCase(s.charAt(0)))
                s = upperCase(s);
           if( trash.contains(s) || tmpQuery.contains(s) || stopWords.contains(lowerCase(s)))
               continue;
           if(s.length()>1)
               res.append(s).append(" ");
        }
        return res;
    }

    private ArrayList<String> createTrashList() {
        ArrayList<String> res = new ArrayList<>();
        res.add("documents");
        res.add("discuss");
        res.add("FIND");
        res.add("find");
        res.add("IDENTIFY");
        res.add("identify");
        res.add("associated");
        res.add("current");
        res.add("provide");
        res.add("background");
        res.add("available");
        res.add("information");
        res.add("INFORMATION");
        return res;
    }

    public void saveSearchResults(String Path) throws IOException {
        ranker.Save_res(Path);
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

}
