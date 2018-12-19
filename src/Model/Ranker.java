//package Model;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.Map;
//import java.util.TreeMap;
//
//import static org.apache.commons.lang3.StringUtils.*;
//
//public class Ranker
//{
//    public TreeMap<String, int[]>   Terms_dictionary;
//    public TreeMap<String, Integer> Docs_dictionary;
//    public ArrayList<Term> query;
//    public double Avg_Doc= 6.8;
//    public ArrayList<String> queryDoc;
//    public ArrayList<String> result;
//    public TreeMap<Double, String> QueryDocRank;
//    public double b=0.75;
//    public double k=2;
//
//    /**
//     * Constructor
//     */
//
//    public Ranker (){
//        QueryDocRank = new TreeMap<>(new Comparator<Double>() {
//            @Override
//            public int compare(Double o1, Double o2) {
//                return o2.compareTo(o1);
//            }
//        });
//        result = new ArrayList<>();
//        queryDoc = new ArrayList<>();
//    }
//
//    public void rankerStart(ArrayList<Term> qList) throws IOException
//    {
//        query = qList;
//        set_result();
//        Build_doc_list();
//        double B25_Rank , Pos_Rank , Total_Rank;
//        System.out.println(queryDoc.size());
//        int index = 0;
//        for (String Doc : queryDoc)
//        {
//            System.out.println(index);
//            index++;
//            B25_Rank =CalculateB25(Doc);
//            Pos_Rank=CalculatePos(Doc);
//
//            Total_Rank =  (B25_Rank+Pos_Rank)/2d ;
//            QueryDocRank.put(Total_Rank,Doc);
//            //System.out.println(Doc);
//        }
//        System.out.println("======================The res=======================");
//
//        print_res();
//
//    }
//
//    public void Build_doc_list(){
//        for (Term term : query)
//        {
//            for(Map.Entry<String,StringBuilder> entry : term.docList.entrySet()){
//                queryDoc.add(entry.getKey());
//                StringBuilder docData = entry.getValue();
//            }
//        }
//    }
//
//
//    private int get_df(String word)
//    {
//        if(Terms_dictionary.containsKey(upperCase(word)))
//            return Terms_dictionary.get(upperCase(word))[0] ;
//        else if(Terms_dictionary.containsKey(lowerCase(word)))
//            return Terms_dictionary.get(lowerCase(word))[0] ;
//        else
//            return 0;
//    }
//
//    private double get_Size_Doc(String Doc) throws IOException
//    {
//        for (Term term : query)
//        {
//            for(Map.Entry<String,StringBuilder> entry : term.docList.entrySet()){
//                if(Doc.equals(entry.getKey()))
//                {
//                    StringBuilder docData = entry.getValue();
//                    String[] st = splitByWholeSeparator(docData.toString(),"," );
//                    return Double.parseDouble(st[1]);
//                }
//            }
//        }
//        return 0d ;
//    }
//
//    private int get_tf(String word ,String Doc) throws IOException {
//        if(Terms_dictionary.containsKey(upperCase(word)))
//            word = upperCase(word);
//        else if(Terms_dictionary.containsKey(lowerCase(word)))
//            word = lowerCase(word);
//        else
//            return 0;
//
//        // if the word is exsit in dictionary
//
//        String Data = get_Data_from_Dic(word);
//        String[] DataArr = splitByWholeSeparator(Data,"~");
//        for(int i=0 ; i<DataArr.length-1 ; i++)
//        {
//            String[] DataWord = splitByWholeSeparator(DataArr[i],",");
//            if(DataWord[0].equals(Doc))
//                return DataWord.length -1 ;
//        }
//        return 0 ;
//    }
//
//
//
//
//    private void print_res()
//    {
//        int index=0;
//        for (Map.Entry<Double, String> entry : QueryDocRank.entrySet())
//        {
//            //if(index<50)
//            // if(result.contains(entry.getValue()))
//            System.out.println(entry.getValue());
//            // else
//            //break;
//            //index++;
//        }
//
//    }
//
//    private double CalculatePos(String doc) throws IOException {
//        double sum = 0;
//
//        for (String term : query)
//            if (get_tf(term, doc) > 0)
//                sum = sum + get_tf(term,doc)*( get_Size_Doc(doc) - get_first_position(term, doc) ) / get_Size_Doc(doc) ;
//
//        return sum;
//    }
//
//    private double CalculateB25(String Doc) throws IOException {
//        double Sum = 0;
//        double logExp;
//        for (Term t : query)
//        {
//            if(get_tf(t,Doc) != 0)
//            {
//                logExp = Math.log10( (Docs_dictionary.size() +1)/(get_df(t)));
//                Sum = Sum + Exp_Calculate_BM25(t,Doc) * logExp ;
//            }
//        }
//
//        return Sum ;
//    }
//
//    private double Exp_Calculate_BM25(String name, String doc) throws IOException {
//        double mone , mechane;
//
//        mone = (k+1) * get_tf(name,doc);
//        mechane = get_tf(name,doc) + k*(1d -b + (b*get_Size_Doc(doc)/Avg_Doc));
//
//        if(mechane == 0)
//            return 0;
//        return (mone/mechane) ;
//    }
//
//
//    private String get_Data_from_Dic(String name) throws IOException {
//        String st;
//        int ptr=0;
//        BufferedReader br;
//
//        if(Terms_dictionary.containsKey(upperCase(name)))
//            ptr=Terms_dictionary.get(upperCase(name))[2] ;
//        else if(Terms_dictionary.containsKey(lowerCase(name)))
//            ptr = Terms_dictionary.get(lowerCase(name))[2] ;
//        if(Character.isLetter(name.charAt(0)))
//        {
//            File file = new File("C:\\Users\\A\\Downloads\\Without_Stemmer\\Without_Stemmer\\"+Character.toUpperCase(name.charAt(0)));
//            br = new BufferedReader(new FileReader(file));
//        }
//        else
//        {
//            File file = new File("C:\\Users\\A\\Downloads\\Without_Stemmer\\Without_Stemmer\\NUM");
//            br = new BufferedReader(new FileReader(file));
//        }
//
//        while(ptr!=0) {
//            br.readLine();
//            ptr--;
//        }
//
//        st=br.readLine();
//        br.close();
//
//        return st;
//
//    }
//
//    private String get_Data(String Doc) throws IOException {
//        String st;
//        int ptr=0;
//        BufferedReader br;
//
//        ptr=Docs_dictionary.get(Doc)-1;
//
//        File file = new File("C:\\Users\\A\\Downloads\\Without_Stemmer\\Without_Stemmer\\mergedDocsPosting");
//        br = new BufferedReader(new FileReader(file));
//
//        while(ptr!=0)
//        {
//            br.readLine();
//            ptr--;
//        }
//        st=br.readLine();
//        br.close();
//
//        return st;
//
//    }
//
//    private ArrayList<String> Get_Doc(String name) throws IOException {
//
//        ArrayList<String> docs_arr = new ArrayList<>();
//        String Data = get_Data_from_Dic(name);
//        String[] DataArr = splitByWholeSeparator(Data,"~");
//        System.out.println(name);
//        for(int i=0 ; i<DataArr.length-1 ; i++)
//        {
//            String[] DataWord = splitByWholeSeparator(DataArr[i],",");
//            docs_arr.add(DataWord[0]);
//            //if(DataWord[0].equals("FBIS3-40520"))
//            //  System.out.println("Yesss");
//
//        }
//        return docs_arr;
//    }
//
//    private double get_first_position(String word ,String Doc) throws IOException {
//        String Data = get_Data_from_Dic(word);
//        String[] DataArr = splitByWholeSeparator(Data,"~");
//        for(int i=0 ; i<DataArr.length-1 ; i++)
//        {
//            String[] DataWord = splitByWholeSeparator(DataArr[i],",");
//            if(DataWord[0].equals(Doc))
//                return Double.parseDouble(DataWord[1]);
//        }
//        return 0;
//    }
//
//    public void set_result()
//    {
//        result.add("FBIS3-20913");
//        result.add("FBIS3-21014");
//        result.add(" FBIS3-21021");
//        result.add("  FBIS3-21022");
//        result.add("  FBIS3-21231");
//        result.add("  FBIS3-21237");
//        result.add(" FBIS3-21239");
//        result.add(" FBIS3-21244");
//        result.add(" FBIS3-23696");
//        result.add("   FBIS3-29781");
//        result.add("  FBIS3-39094");
//        result.add("  FBIS3-40346");
//        result.add("  FBIS3-40351");
//        result.add(" FBIS3-40520");
//        result.add("FBIS3-40521");
//        result.add(" FBIS3-40550");
//        result.add("FBIS3-42376");
//        result.add("FBIS3-42531");
//        result.add(" FBIS3-42535");
//        result.add("  FBIS3-42541");
//        result.add("  FBIS3-42544");
//        result.add("  FBIS3-56306");
//        result.add(" FBIS3-56960");
//        result.add(" FBIS3-59677");
//        result.add("   FBIS3-59774");
//        result.add("  FBIS3-59901");
//        result.add(" FBIS4-28821");
//        result.add("     FBIS4-44690");
//        result.add(" FBIS4-44785");
//        result.add("  FBIS4-44844");
//        result.add("  FBIS4-44870");
//        result.add("      FBIS4-44900");
//        result.add("     FBIS4-62583");
//        result.add("    FBIS4-66418");
//        result.add("    FT921-5383");
//        result.add("    FT933-5505");
//        result.add("FT934-10103 ");
//        result.add("  FT943-15250");
//        result.add("  LA040289-0050");
//        result.add("   LA051290-0077");
//        result.add("  LA070290-0143");
//        result.add(" LA071090-0133");
//        result.add("   LA072189-0108");
//        result.add(" LA072190-0065");
//        result.add("   LA072289-0042");
//        result.add(" LA083090-0083");
//        result.add(" LA091189-0054");
//        result.add(" LA092990-0085");
//        result.add("  LA092990-0100");
//        result.add("  LA102289-0040");
//        result.add("  LA121190-0089");
//    }
//}
