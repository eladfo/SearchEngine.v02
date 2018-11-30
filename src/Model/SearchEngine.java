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

    public SearchEngine(String corpusPath, String postPath, Boolean isStemm) throws IOException {
        this.corpusPath = corpusPath;
        this.postPath = postPath;
        partiotions = 20;
        rf = new ReadFile(corpusPath);
        idx = new Indexer(postPath, partiotions, isStemm);
        docs=null;
        //partiotions = rf.getListOfFiles().length/50;
        parse = new Parse(isStemm);
    }

    public void createSearchEngine() throws IOException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < partiotions; i++) {
            long p0 = System.currentTimeMillis();
            //docs.clear();
            docs = rf.readLines(i);
            int j=0;
            for (Doc d : docs)
            {
                ParsedDoc pd = parse.run(d);
                idx.addParsedDoc(pd);
                d.docText.setLength(0);
            }
           // idx.createTmpCityPosting(i);
            idx.createTermTmpPosting(i);
            docs.clear();
            idx.resetIndex();
            //            long p0 = System.currentTimeMillis();
            long p1 = System.currentTimeMillis();
            System.out.println(p1 - p0);
        }
        idx.createInvertedIndex();

        long endTime = System.currentTimeMillis();
        System.out.println("***********");
        System.out.println(endTime - startTime);
        System.out.println("***********");
    }
public String getpath()
{
    return corpusPath;
}
}
