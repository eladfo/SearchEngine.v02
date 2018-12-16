package Model;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class SearchEngine {
    ReadFile rf;
    Parse parse;
    public Indexer idx;
    HashSet<Doc> docs;
    int partiotions;
    int indexedDocs;
    int uniqueTerms;
    double totalRunTime;

    /**
     * Constructor. Initialize 3 paths for corpus, posting and stopwords.
     * Calculate the partition amount, by dividing the corpus size by 50.
     */
    public SearchEngine(String corpusPath, String postPath, Boolean isStemm, String stopwordsPath) throws IOException {
        rf = new ReadFile(corpusPath);
        partiotions = (int) Math.ceil(rf.getListOfFilesSize()/50.0);
        idx = new Indexer(postPath, partiotions, isStemm);
        parse = new Parse(corpusPath, isStemm,stopwordsPath);
        docs = new HashSet<>();
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
                idx.addParsedDoc(pd);
                d.getDocText().setLength(0);
            }
            idx.createTmpPosting(i);
            idx.resetIndex();
            long p1 = System.currentTimeMillis();
//            System.out.println(i + " "+ (p1 - p0));
        }
        idx.createInvertedIndex();
        long endTime = System.currentTimeMillis();
        totalRunTime = (endTime - startTime) / 1000;
        uniqueTerms = idx.finalTermsDic.size();
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
        idx.resetIndex();
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
}
