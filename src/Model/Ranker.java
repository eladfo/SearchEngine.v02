package Model;

import java.io.*;
import java.util.*;
import static org.apache.commons.lang3.StringUtils.*;

public class Ranker
{
    public Indexer idx ;
    public ArrayList<Term> query;
    public double avgDocLength = 443.4632;
    public double num_docs_crorpus= 472525;
    public ArrayList<String> queryDoc;
    public ArrayList<String> result;
    public ArrayList<String> result_tmp ;
    public TreeMap<Double, String> QueryDocRank;
    public double b=0.75;
    public double k=1.2;
    public ArrayList<String> semantic_words;
    public HashMap<String, ArrayList<String[]>> info_map ;

    public Ranker ()
    {
        result_tmp = new ArrayList<>();
        QueryDocRank = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }
        });
        result = new ArrayList<>();
        queryDoc = new ArrayList<>();
        semantic_words = new ArrayList<>();
    }

    public ArrayList<String> rankerStart(String path , String num_query, ArrayList<Term> qList , Indexer indexer , HashMap<String, ArrayList<String[]>> map) throws IOException
    {
        double B25_Rank =0  , Total_Rank = 0 ,CosSim_Rank = 0;
        double mone = 0 , mechane=0 , sqr = 0;
        info_map = map;
        idx = indexer;
        query = qList;

        Reset();
        for (Map.Entry<String, ArrayList<String[]>> entry : map.entrySet())
        {
            for(String[] Data : entry.getValue())
            {
                B25_Rank = B25_Rank + CalculateB25(Double.valueOf(Data[1]) , Double.valueOf(Data[2]) , entry.getKey());


                mone = mone + Double.valueOf(Data[1]);
                mechane = mechane + Math.pow(Double.valueOf(Data[1]),2);
            }
            sqr = Math.sqrt(mechane);
            CosSim_Rank =  mone/sqr;
            Total_Rank = B25_Rank*0.9  + CosSim_Rank*0.1 ;

            //Header_Rank();
            QueryDocRank.put(Total_Rank, entry.getKey());
            B25_Rank = 0 ;
            mechane=0;
            mone=0;
            CosSim_Rank=0;
        }
        Addres(num_query);

       // Build_doc_list();
        //Header_Rank();
       // for (String Doc : queryDoc)
       // {
           // B25_Rank =CalculateB25(Doc);
           // CosSim_Rank =CalculateCosSim(Doc);
           // Total_Rank = B25_Rank*0.7  + CosSim_Rank*0.3 ;
         //   QueryDocRank.put(Total_Rank,Doc);
       // }
        //Addres(num_query);

        return result;

    }

    private void Header_Rank()
    {
        String[] data;
        for (Term t : query)
        {
            for (Map.Entry<String, StringBuilder> entry : t.docList.entrySet())
            {
                data = split(entry.getValue().toString() , ",");
                for(String s : data)
                {
                    if(s.equals("0"))
                    {
                        QueryDocRank.put(1000d,entry.getKey());
                        queryDoc.remove(entry.getKey());
                        break;
                    }
                }

            }

        }
    }


    private double CalculateB25(double tf , double df , String doc) throws IOException {
        double Sum = 0;
        double logExp;

        if(tf != 0)
        {
            logExp = Math.log10( (num_docs_crorpus +1)/(df));
            // sizde doc + 1 ==> num of docs in corpus + 1
            Sum = Sum + Exp_Calculate_BM25(tf,doc) * logExp ;
        }

        return Sum ;
    }

    private double Exp_Calculate_BM25(double tf, String doc) throws IOException {
        double mone , mechane;
        mone = (k+1) * tf;
        mechane = tf + k*(1d -b + (b*get_Size_Doc(doc)/ avgDocLength));
        if(mechane == 0)
            return 0;
        return (mone/mechane) ;
    }

    private double get_Size_Doc(String Doc) throws IOException
    {
        int[] arr = idx.finalDocsDic.get(Doc);
        return (double)arr[0];
    }

    public void Save_res(String postingsPath) throws IOException
    {
        int index=0;
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

    private void Addres(String numqurey)
    {
        int index=0;
        int good = 0;
        for (Map.Entry<Double, String> entry : QueryDocRank.entrySet())
        {
            if(index<50)
            {
                System.out.println(numqurey+"~"+entry.getValue());
                result.add(numqurey+"~"+entry.getValue());
                if(result_tmp.contains(entry.getValue()))
                    good++;
            }
            else
                break;
            index++;
        }
        System.out.println(good +  "   hhh") ;
    }



   /*
   public void rankerStartBeta(String path , String num_query, ArrayList<Term> qList , Indexer indexer) throws IOException
    {
        idx = indexer;
        //set_result();
        query = qList;
        double B25_Rank  , Total_Rank,CosSim_Rank;
        for (Term term : query) {
            for (Map.Entry<String, StringBuilder> entry : term.docList.entrySet()) {
                String docID = entry.getKey();
                StringBuilder docData = entry.getValue();
                String[] s = split(docData.toString(), ";");
                double docSize =  Double.parseDouble(s[1]);
                double termTF =  Double.parseDouble(s[0]);
                double termDF = term.docList.size();

                B25_Rank = CalculateB25(termDF, termTF , docSize);
                CosSim_Rank = CalculateCosSim(Doc);
                Total_Rank = B25_Rank * 0.7 + CosSim_Rank * 0.3;
                QueryDocRank.put(Total_Rank, docID);
            }
            Addres(num_query);
        }
    }

*/


    public void Reset()
    {
        queryDoc.clear();
        QueryDocRank.clear();
    }

}
