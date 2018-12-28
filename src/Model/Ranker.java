package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Ranker
{
    public Indexer idx ;
    public ArrayList<Term> query;
    public double Avg_Doc= 600;
    public double num_docs_crorpus= 472525;
    public ArrayList<String> queryDoc;
    public ArrayList<String> result;
    public ArrayList<String> result_tmp ;
    public TreeMap<Double, String> QueryDocRank;
    public double b=0.75;
    public double k=1.2;
    public ArrayList<String> semantic_words;

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

    public void rankerStart(String path , String num_query, ArrayList<Term> qList , Indexer indexer) throws IOException
    {
        idx = indexer;
        set_result();
        query = qList;
        Build_doc_list();
        double B25_Rank  , Total_Rank,CosSim_Rank;
        for (String Doc : queryDoc)
        {
            B25_Rank =CalculateB25(Doc);
            CosSim_Rank =CalculateCosSim(Doc);
            Total_Rank =    B25_Rank*0.7 + CosSim_Rank*0.3 ;
            QueryDocRank.put(Total_Rank,Doc);
        }
        Addres(num_query);
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
            if(index<50)
            {
                   s.append(arr[0]).append(" 0 ").append(arr[1]).append(" 1 42.38 mt\n");
                   bw.write(s.toString());
                   s.setLength(0);
            }
            else
                break;
            index++;
        }
        bw.close();
        fw.close();
    }

    public void Build_doc_list(){
        for (Term term : query)
        {
            for(Map.Entry<String,StringBuilder> entry : term.docList.entrySet())
                queryDoc.add(entry.getKey());
        }
    }


    private double get_df(Term t)
    {
        return t.docList.size();
    }

    private double get_Size_Doc(String Doc) throws IOException
    {
        int[] arr = idx.finalDocsDic.get(Doc);
        return (double)arr[0];
    }

    private double get_tf(Term t ,String Doc) throws IOException
    {
        for (Map.Entry<String, StringBuilder> entry : t.docList.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(Doc)) {
                StringBuilder docData = entry.getValue();
                String[] s = split(docData.toString(), ";");
                return Double.parseDouble(s[0]);
            }
        }
        return 0d;
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


    private double CalculateCosSim(String doc) throws IOException {
        double mone = 0;
        double mechane = 0;
        double tf;
        for (Term t : query)
        {
            tf = get_tf(t, doc);
            mone = mone + tf;
            mechane = mechane + Math.pow(tf,2);
        }
        double sqr = Math.sqrt(mechane);
        return mone/sqr;
    }

    private double CalculateB25(String doc) throws IOException {
        double Sum = 0;
        double logExp;
        double df;
        double tf;
        for (Term t : query)
        {
            tf = get_tf(t,doc);
            df = get_df(t);
            if(get_tf(t,doc) != 0)
            {
                logExp = Math.log10( (num_docs_crorpus +1)/(df));
                // sizde doc + 1 ==> num of docs in corpus + 1
                Sum = Sum + Exp_Calculate_BM25(tf,doc) * logExp ;
            }
        }
        return Sum ;
    }

    private double Exp_Calculate_BM25(double tf, String doc) throws IOException {
        double mone , mechane;
        mone = (k+1) * tf;
        mechane = tf + k*(1d -b + (b*get_Size_Doc(doc)/Avg_Doc));
        if(mechane == 0)
            return 0;
        return (mone/mechane) ;
    }

    public void set_result() throws IOException {
        String st;
        String [] ss ;
        BufferedReader brTermPost = new BufferedReader(new FileReader(new File
                ("C:\\Users\\A\\Downloads\\Searcher2\\Searcher\\British Chunnel impact.txt")));
        while((st= brTermPost.readLine())!=null )
        {
            ss = split(st," ");
            if(ss[3].equals("1"))
            {
                //System.out.println(ss[2]);
                result_tmp.add(ss[2]);
            }
        }
    }

    public void Reset()
    {
        QueryDocRank.clear();
    }




 /*
        private double get_tf_semantica(String word ,String Doc) throws IOException
        {
            double Sum=0;
            semantic_words.clear();
            Get_semantica(word);
            for(String st :semantic_words )
                Sum = Sum + get_tf(st,Doc);
            Sum = Sum + get_tf(word,Doc);
            return Sum;
        }
    */
//    public void  Get_semantica (String word) throws IOException {
//
//        URL url = new URL("https://api.datamuse.com/words?ml="+word);
//        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestMethod("GET");
//        InputStreamReader input_stem =  new InputStreamReader(con.getInputStream());
//        BufferedReader in = new BufferedReader(input_stem);
//        String inputLine;
//        StringBuffer content = new StringBuffer();
//        while ((inputLine = in.readLine()) != null)
//        {
//            content.append(inputLine);
//        }
//        parse_semantica(content);
//        in.close();
//        con.disconnect();
//    }
//
//    private ArrayList<String> parse_semantica(StringBuffer content)
//    {
//        String[] st = splitByWholeSeparator(content.toString(),"},{");
//        String[] st1 ;
//        String[] st2;
//        char c;
//        for(int i=0 ; i<5 && i<st.length ;i++)
//        {
//            st1 = splitByWholeSeparator(st[i],",");
//            st2=splitByWholeSeparator(st1[0],":");
//            c = '"';
//            st2[1] = replaceChars(st2[1],c,'*');
//            st2[1] = replace(st2[1],"*","");
//            // System.out.println(st2[1]);
//            semantic_words.add(st2[1]);
//        }
//        return semantic_words;
//    }
}
