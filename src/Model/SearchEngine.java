package Model;
import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.contains;
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

    /**
     * Constructor. Initialize 3 paths for corpus, posting and stopwords.
     * Calculate the partition amount, by dividing the corpus size by 50.
     */
    public SearchEngine(String corpusPath, String postPath, Boolean isStemm, String stopwordsPath) throws IOException {
        rf = new ReadFile(corpusPath);
        if(isStemm)
            postingsPath = postPath + "\\With_Stemmer";
        else
            postingsPath = postPath + "\\Without_Stemmer";
        partiotions = (int) Math.ceil(rf.getListOfFilesSize()/50.0);
        //partiotions=2;
        stemmFlag = isStemm;
        index = new Indexer(postingsPath, partiotions);
        parse = new Parse(corpusPath, stemmFlag, stopwordsPath);
        docs = new HashSet<>();
        ranker = new Ranker();

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
                ParsedDoc pd = parse.runParser(d);
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

    public void Reset(String path) {
        rf.resetDocSet();
        parse.resetParse();
        index.resetIndex();
        deleteAllFiles(path);
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

    public void partB(String qPath, boolean cityFlag, boolean semanticFlag) throws IOException {
        index.loadDics(postingsPath);
        search = new Searcher(cityFlag, semanticFlag, index);
        HashMap<String,StringBuilder> querys = rfBeta(qPath);


        for (Map.Entry<String, StringBuilder> entry : querys.entrySet()) {
            search.createTermsList(entry.getValue().toString(), postingsPath);
            ranker.rankerStart(postingsPath ,entry.getKey(),search.getQueryTerms());
        }
    }

    public void save_res() throws IOException {
        ranker.Save_res(postingsPath);
    }

    public HashMap<String,StringBuilder> rfBeta(String qPath) throws IOException {   //function that Readfile Query file!!!
        HashMap<String,StringBuilder> res = new HashMap<>();
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
                ParsedDoc pd = parse.runParser(d);
                StringBuilder sb = new StringBuilder();
                for(Map.Entry<String, StringBuilder> entry : pd.getTerms().entrySet())
                    sb.append(entry.getKey()).append(" ");
                res.put(num_query,sb);
            }
        }
        return res;
    }
}
