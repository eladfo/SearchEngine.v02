package Model;

import java.io.IOException;
import java.util.HashSet;

public class SearchEngine {
    ReadFile rf;
    Parse parse;
    Indexer idx;
    HashSet<Doc> docs;
    int partiotions;
    String corpusPath;
    String postPath;
    int indexedDocs;
    int uniqueTerms;
    double totalRunTime;

    public SearchEngine(String corpusPath, String postPath, Boolean isStemm) throws IOException {
        this.corpusPath = corpusPath;
        this.postPath = postPath;
//        partiotions = 3;
        rf = new ReadFile(corpusPath);
        partiotions = (int) Math.ceil(rf.getListOfFiles().length/50.0);
        idx = new Indexer(postPath, partiotions, isStemm);
        parse = new Parse(isStemm);
        docs = new HashSet<>();
    }

    public void createSearchEngine() throws IOException {
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
        totalRunTime = (endTime - startTime)/1000;

        uniqueTerms = idx.finalTermsDic.size();
        printResults();
    }

    public void printResults(){
        System.out.println("*~*~*~*~*~*~*~*~*~*~*");
        System.out.println(indexedDocs + " :: Docs");
        System.out.println(uniqueTerms + " :: Terms");
        System.out.println(totalRunTime + " :: Total RT");
        System.out.println("*~*~*~*~*~*~*~*~*~*~*");
    }

    public String get_num_term(){return String.valueOf(uniqueTerms); }
    public String get_num_doc(){return String.valueOf(indexedDocs);}
    public String get_num_rt(){return String.valueOf(totalRunTime);}

    public void Reset()
    {
        rf.Reset();
        parse.resetParse();
        idx.resetIndex();
        idx.deleteTmpFiles(0);
        docs.clear();
    }


}
