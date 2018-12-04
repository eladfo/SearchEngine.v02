package Model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.substring;

public class SearchEngine {
    ReadFile rf;
    Parse parse;
    public Indexer idx;
    HashSet<Doc> docs;
    int partiotions;
    int indexedDocs;
    int uniqueTerms;
    double totalRunTime;
    String pp;

    public SearchEngine(String corpusPath, String postPath, Boolean isStemm) throws IOException {
        pp = postPath;
        partiotions = 2;
        rf = new ReadFile(corpusPath);
//        partiotions = (int) Math.ceil(rf.getListOfFilesSize()/50.0);
        idx = new Indexer(postPath, partiotions, isStemm);
        parse = new Parse(corpusPath, isStemm);
        docs = new HashSet<>();
    }


    public void runSearchEngine() throws IOException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < partiotions; i++) {
            long p0 = System.currentTimeMillis();
            docs.clear();
            docs = rf.readLines(i);
            indexedDocs += docs.size();
            for (Doc d : docs) {
                ParsedDoc pd = parse.run(d);
                idx.addParsedDoc(pd);
                d.docText.setLength(0);
            }
            idx.createTmpPosting(i);
            idx.resetIndex();

            long p1 = System.currentTimeMillis();
            System.out.println(p1 - p0);
        }
        idx.createInvertedIndex();
        long endTime = System.currentTimeMillis();
        totalRunTime = (endTime - startTime) / 1000;

        uniqueTerms = idx.finalTermsDic.size();
        printResults();
    }

    public void printResults() {
        System.out.println("*~*~*~*~*~*~*~*~*~*~*");
        System.out.println(indexedDocs + " :: Docs");
        System.out.println(uniqueTerms + " :: Terms");
        System.out.println(totalRunTime + " :: Total RT");
        System.out.println("*~*~*~*~*~*~*~*~*~*~*");
    }

    public String get_num_term() {
        return String.valueOf(uniqueTerms);
    }

    public String get_num_doc() {
        return String.valueOf(indexedDocs);
    }

    public String get_num_rt() {
        return String.valueOf(totalRunTime);
    }

    public void Reset(String path) {
        rf.resetDocSet();
        parse.resetParse();
        idx.resetIndex();
        deleteAll(path);
        docs.clear();
    }

    private void deleteAll(String path){
        File directory = new File(path);
        if(directory.listFiles() != null) {
            for (File dir : directory.listFiles()) {
                File innerDir = new File(dir.getPath());
                if(innerDir.listFiles() != null) {
                    for (File file : innerDir.listFiles()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
