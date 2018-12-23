package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class Ranker
{
    public ArrayList<Term> query;
    public double Avg_Doc= 600;
    public ArrayList<String> queryDoc;
    public ArrayList<String> result;
    public TreeMap<Double, String> QueryDocRank;
    public double b=0.75;
    public double k=1.2;
    public double doclength = 472525;
    public ArrayList<String> semantic_words;

    public Ranker ()
    {
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

    public void rankerStart(ArrayList<Term> qList) throws IOException
    {
        query = qList;
        set_result();
        Build_doc_list();
        double B25_Rank  , Total_Rank,CosSim_Rank;
        System.out.println(queryDoc.size());
        for (String Doc : queryDoc)
        {
            B25_Rank =CalculateB25(Doc);
            CosSim_Rank =CalculateCosSim(Doc);
            Total_Rank =    B25_Rank*0.7 + CosSim_Rank*0.3 ;
            QueryDocRank.put(Total_Rank,Doc);
        }
        System.out.println("======================The res=======================");

        print_res();
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
        for (Term term : query)
        {
            for(Map.Entry<String,StringBuilder> entry : term.docList.entrySet()){
                if(Doc.equals(entry.getKey()))
                {
                    StringBuilder docData = entry.getValue();
                    String[] st = splitByWholeSeparator(docData.toString(),";" );
                    return Double.parseDouble(st[1]);
                }
            }
        }
        return 0d ;
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
    private void print_res()
    {
        int index=0;
        int good=0;
        for (Map.Entry<Double, String> entry : QueryDocRank.entrySet())
        {
            if(index<50) {
                if (result.contains(entry.getValue()))
                    good++;
            }
            else
                break;
            index++;
        }
        System.out.println("The total good res:  " +  result.size() );
        System.out.println("The good rank is:  " +  (double)good);

    }

    private double CalculateCosSim(String doc) throws IOException {
        double mone = 0;
        double mechane = 0;
        for (Term t : query)
        {
            mone = mone + get_tf(t, doc);
            mechane = mechane + Math.pow(get_tf(t, doc),2);
        }
        double sqr = Math.sqrt(mechane);
        return mone/sqr;
    }

    private double CalculateB25(String doc) throws IOException {
        double Sum = 0;
        double logExp;
        for (Term t : query)
        {
            if(get_tf(t,doc) != 0)
            {
                logExp = Math.log10( (get_Size_Doc(doc) +1)/(get_df(t)));
                Sum = Sum + Exp_Calculate_BM25(t,doc) * logExp ;
            }
        }
        return Sum ;
    }

    private double Exp_Calculate_BM25(Term t, String doc) throws IOException {
        double mone , mechane;
        mone = (k+1) * get_tf(t,doc);
        mechane = get_tf(t,doc) + k*(1d -b + (b*get_Size_Doc(doc)/Avg_Doc));
        if(mechane == 0)
            return 0;
        return (mone/mechane) ;
    }

    public void set_result() throws IOException {
        String st;
        String [] ss ;
        BufferedReader brTermPost = new BufferedReader(new FileReader(new File
                ("C:\\Users\\alonts\\Downloads\\Searcher\\Searcher\\British Chunnel impact.txt")));
        while((st= brTermPost.readLine())!=null )
        {
            ss = split(st," ");
            if(ss[3].equals("1"))
            {
                System.out.println(ss[2]);
                result.add(ss[2]);
            }
        }
    }

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
