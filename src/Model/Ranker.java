package Model;

import java.io.*;
import java.util.*;
import static org.apache.commons.lang3.StringUtils.*;

public class Ranker
{
    public Indexer idx ;
    public double avgDocLength = 443.4832;
    public double num_docs_crorpus= 472525;
    public ArrayList<String> queryDoc;
    public ArrayList<String> result;
    public TreeMap<Double, String> QueryDocRank;
    public ArrayList<String> semantic_words;
    public final double b=0.38; //0.38
    public final double k=1.2; // 1.2
    public final double alpha = 1;
    public final double beta = 10;
    public final double gama = 0;
    public final double delta = 0;

    /**
     * Ranker's constructor.
     */
    public Ranker ()
    {
        QueryDocRank = new TreeMap<>(Comparator.reverseOrder());
        result = new ArrayList<>();
        queryDoc = new ArrayList<>();
        semantic_words = new ArrayList<>();
    }

    /**
     * Ranking all docs speared in 'map', by calculating score using the unique formula.
     * Formula mainly contain, BM25, CosSimilarty, doc's header and query's title score.
     * Each element get its own weight, to maximize the top 50 relents docs.
     * @param num_query
     * @param originalQ - words which appear in original query's title.
     * @param indexer
     * @param map - gets from Seacher.
     * @return List of top 50 most relevant docs per query.
     * @throws IOException
     */
    public ArrayList<String> rankerStart(String num_query, String originalQ, Indexer indexer, HashMap<String, ArrayList<String[]>> map) throws IOException
    {
        double B25_Rank = 0  , Total_Rank = 0 ,CosSim_Rank = 0 , Header_Rank=0;
        double mone = 0 , mechane=0 , sqr = 0, originWordsRank =0;
        idx = indexer;
        boolean is_header = false;
        Reset();
        String[] tokens = split(originalQ, " ");
        ArrayList<String> origin = new ArrayList<>(Arrays.asList(tokens));
        for (Map.Entry<String, ArrayList<String[]>> entry : map.entrySet())
        {
            for(String[] data : entry.getValue())
            {
                if(origin.contains(data[0]))
                    originWordsRank = originWordsRank + 2*calculateBM25(Double.valueOf(data[1]) , Double.valueOf(data[2]) , entry.getKey());
                B25_Rank = B25_Rank + calculateBM25(Double.valueOf(data[1]) , Double.valueOf(data[2]) , entry.getKey());
                mone = mone + Double.valueOf(data[1]);
                mechane = mechane + Math.pow(Double.valueOf(data[1]),2);
                if(data[3].equalsIgnoreCase("0")) {
                    is_header = true;
                }
            }
            if(is_header){
                Header_Rank = 1d;
            }
            sqr = Math.sqrt(mechane);
            CosSim_Rank =  mone/sqr;

            Total_Rank = B25_Rank*alpha + Header_Rank*beta + CosSim_Rank*gama + originWordsRank*delta;

            QueryDocRank.put(Total_Rank, entry.getKey());
            B25_Rank = 0 ;
            mechane=0;
            mone=0;
            CosSim_Rank=0;
            Header_Rank=0;
            is_header = false;
        }
        createTop50Results(num_query);
        return result;
    }

    private double calculateBM25(double tf , double df , String doc){
        double Sum = 0;
        double IDFq;

        if(tf != 0)
        {
            IDFq = Math.log((num_docs_crorpus - df + 0.5)/(df+0.5));
            Sum = Sum + calc_bm25_exp(tf,doc) * IDFq ;
        }
        return Sum ;
    }

    private double calc_bm25_exp(double tf, String doc){
        double mone , mechane;
        mone = (k+1) * tf;
        mechane = tf + k*(1d -b + (b* get_doc_size(doc)/ avgDocLength));
        if(mechane == 0)
            return 0;
        return (mone/mechane) ;
    }

    private double get_doc_size(String Doc)
    {
        int[] arr = idx.finalDocsDic.get(Doc);
        return (double)arr[0];
    }

    /**
     * Gets a query's number, and update maximum 50 best results to the docs rank data structure.
     * @param numqurey
     */
    private void createTop50Results(String numqurey)
    {
        int index=0;
        for (Map.Entry<Double, String> entry : QueryDocRank.entrySet())
        {
            if(index<50)
            {
                result.add(numqurey+"~"+entry.getValue());
            }
            else
                break;
            index++;
        }
    }

    /**
     * Saving queries results with the format for Treceval.
     * @param postingsPath - path to save ranker results.
     * @throws IOException
     */
    public void saveRankerResults(String postingsPath) throws IOException
    {
        FileWriter fw = new FileWriter(postingsPath + "\\results.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        StringBuilder s = new StringBuilder();
        String[] arr;
        for (String line : result)
        {
            arr = split(line , "~");
            s.append(arr[0]).append(" 0 ").append(arr[1]).append(" 1 42.38 mt\n");
            bw.write(s.toString());
            s.setLength(0);
        }
        bw.close();
        fw.close();
    }


    public void Reset()
    {
        queryDoc.clear();
        QueryDocRank.clear();
    }

}
