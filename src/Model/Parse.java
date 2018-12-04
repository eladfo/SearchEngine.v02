package Model;//package Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.*;

public class Parse
{
    private String[] stk;
    private int index;
    private  boolean isStem;
    private ParsedDoc parsedDoc = new ParsedDoc();
    private StringBuilder strb = new StringBuilder();
    private HashMap<String,String> month = new HashMap<>() ;
    private HashSet<String> stop_words = new HashSet<>();
    private HashSet<String> tmp_word = new HashSet<>();
    public int position_of_word;
    public TreeMap<String, StringBuilder> Capital_City;
    public Stemmer stemmer;

    public Parse(String corpusPath, Boolean stemm) throws IOException {
        isStem = stemm;
        set_month();
        set_tmp_word();
        if(corpusPath != "") {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v02\\resources" + "\\stop_words.txt"));
            String st;
            while ((st = br.readLine()) != null) {
                if (!st.isEmpty())
                    stop_words.add(lowerCase(initialParse(st)));
            }
        }
        Capital_City = new TreeMap<>();
        Create_City_Map();
        stemmer = new Stemmer();
    }

    private void set_tmp_word() {
        tmp_word.add("thousand");
        tmp_word.add("million");
        tmp_word.add("trillion");
        tmp_word.add("billion");
        tmp_word.add("percent");
        tmp_word.add("percentage");
        tmp_word.add("dollars");
        tmp_word.add("m");
        tmp_word.add("bn");
        tmp_word.add("billion");
        tmp_word.add("million");
        tmp_word.add("trillion");
    }

    private void set_month() {
        month.put("january","01");
        month.put("february","02");
        month.put("march","03");
        month.put("april","04");
        month.put("may","05");
        month.put("june","06");
        month.put("july","07");
        month.put("august","08");
        month.put("september","09");
        month.put("october","10");
        month.put("november","11");
        month.put("december","12");
    }

    public void Add_term(StringBuilder sb, int pos) {

        if(isStem)
            parsedDoc.addTerm(stemmer.stem(sb.toString()), pos);
        else
            parsedDoc.addTerm(sb.toString(), pos);
        position_of_word++;
    }

    public ParsedDoc run(Doc doc) throws IOException
    {
            parsedDoc = new ParsedDoc();
            parsedDoc.setDocID(doc.getDocID());
            stk = split(doc.getDocText().toString(), " ():[];?/]=");
            index = 0;                 // index of word in file include stop words!
            position_of_word = 1;      // index of word in file without stop words!

        while (index < stk.length) {
                String s = initialParse(stk[index]);
                if (s.length() == 0 || stop_words.contains(lowerCase(s))) {
                    index++;
                    continue;
                }
                strb.setLength(0);
                if (s.charAt(0) >= '0' && s.charAt(0) <= '9')
                {
                    if (s.charAt(s.length() - 1) == '%') {
                        Add_term(strb.append(s), position_of_word);
                    } else if (!is_fraction(s)) {
                        tokenIsNum(s);
                    } else if (index + 1 < stk.length && equalsIgnoreCase(stk[index + 1], "Dollars")) {
                        Add_term(strb.append(s).append(" Dollars"), position_of_word);
                    }
                } else if (Character.isLetter(s.charAt(0))) {
                    if (index + 1 < stk.length && month.containsKey(lowerCase(s)) && isNumeric(stk[index + 1])) {
                        Add_term(strb.append(month.get(lowerCase(s))).append("-").append(is_under_10(initialParse(stk[index + 1]))), position_of_word);
                        index++;
                        continue;
                    }
                    else
                    {
                        if(contains(s,"--"))
                        {
                            String[] tmp =split(s, "--");
                            for(int j=0 ; j<tmp.length;j++)
                            if(stop_words.contains(lowerCase(tmp[j])))
                                continue;
                        /**
                         StringIndexOutOfBoundsException* }
                        else if(contains(s,"-")) {
                            Add_term(strb.append(s), position_of_word);
                            String[] tmp = split(s, "-");
                            for(String ss : tmp)
                            {
                                position_of_word--;
                                strb.setLength(0);
                                Add_term(strb.append(initialParse(ss)), position_of_word);
                            }
                         */
                        } else
                            Add_term(strb.append(s), position_of_word);
                    }
                }
                strb.setLength(0);
                index++;
            }
        Create_City_Posting(doc.getDocCity());
        parsedDoc.fileID = doc.docFile;
        return parsedDoc;
    }

    private String is_under_10(String s)    {
        if( !is_float(s)&& Integer.parseInt(s)<10)
            return ("0"+s) ;
        return s;
    }

    private String initialParse(String s) {
        char tmp = '"';
        s= replaceChars(s,tmp,'#');
        s=replaceChars(s,"'#+<>|~,!�","");

        if (org.apache.commons.lang3.StringUtils.contains(s,".") && !s.equals("U.S.") && !Character.isDigit(s.charAt(0)) && s.charAt(0)!='$')
          s=  replaceChars(s,".","");
        else if (s.length()>0 && s.charAt(s.length()-1)  == '.' && !s.equals("U.S.") )
            s=replaceChars(s,".","");
        if(s.length()>0 && (s.charAt(s.length()-1)  == '-'))
            s=replaceChars(s,"-","");
        else if(s.length()>0 &&  s.charAt(s.length()-1)  == '*')
            s=replaceChars(s,"*","");
        if(s.length()>0 &&  s.charAt(0) == '`')
           s= replaceChars(s,"`","");
        return s;
    }

    private void tokenIsNum(String s) {
        if(contains(s,"-")) {
            Add_term(strb.append(s),position_of_word);
            return;
        }

        float f;
       if(isNumeric(s)||is_float(s))
           f = Float.parseFloat(s);
        else
       {
           /*
           int j;
           for (j = 0 ; j < s.length() && !(Character.isLetter(s.charAt(j))) ;j++);
          // System.out.println(substring(s,j,s.length()));
           if(j<s.length()-1)
           {
               Add_term(strb.append(substring(s,0,j)),position_of_word);
               strb.setLength(0);
               Add_term(strb.append(substring(s,j,s.length())),position_of_word+1);
               position_of_word++;
           }
           else
           Add_term(strb.append(s),position_of_word);
           */
           return;

       }

        Boolean flag = false;
        if (index + 1 < stk.length && tmp_word.contains(lowerCase(stk[index + 1]))) {
            String nextTkn = stk[index + 1];
            flag = checkNextTkn(f, nextTkn);
        }

        if (!flag) {
            if (f < 1000) {
                Add_term(strb.append(is_under_10(from_number_to_string(f))),position_of_word);
            } else if (f < 1000000 && f >= 1000) {
                f = f / 1000;
                Add_term(strb.append(from_number_to_string(f)).append("k"),position_of_word);
            } else if (f < 1000000000 && f >= 1000000) {
                f = f / 1000000;
                Add_term(strb.append(from_number_to_string(f)).append("M"),position_of_word);
            } else if (f >= 1000000000) {
                f = f / 1000000000;
                Add_term(strb.append(from_number_to_string(f)).append("B"),position_of_word);
            }
        }
    }

    private boolean is_float(String s)    {
        try {
            float f = Float.parseFloat(s);
            return true;
        }catch (NumberFormatException e) {
            return false;
        }
    }

    private Boolean checkNextTkn(float f, String nextTkn) {

        if (equalsIgnoreCase(nextTkn,"Thousand")) {
            Add_term(strb.append(from_number_to_string(f)).append("k"),position_of_word);
            index++;
            return true;
        }
        else if (is_Date(f,nextTkn) != null) {
            Add_term(strb.append(is_Date(f,nextTkn)).append("-").append(from_number_to_string(f)),position_of_word);
            index++;
            return true;
        }else if (equalsIgnoreCase(nextTkn,"Million")) {
            Add_term(strb.append(from_number_to_string(f)).append("M"),position_of_word);
            index++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"Billion")) {
            Add_term(strb.append(from_number_to_string(f)).append("B"),position_of_word);
            index++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"Trillion")) {
            Add_term(strb.append(from_number_to_string(f*1000)).append("B"),position_of_word);
            index++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"percent") || nextTkn.equals("percentage")) {
            Add_term(strb.append(from_number_to_string(f)).append("%"),position_of_word);
            index++;
            return true;
        }
        else if (f < 1000000 && equalsIgnoreCase(nextTkn,"Dollars")) {
            Add_term(strb.append(from_number_to_string(f)).append(" ").append(nextTkn),position_of_word);
                index++;
                return true;
        }
        else if (is_fraction(nextTkn)) {
            if (index + 2 < stk.length && stk[index + 2].equals("Dollars")) {
                Add_term(strb.append(from_number_to_string(f)).append(" ").append(nextTkn).append("Dollars"),position_of_word);
                index = index + 2;
                return true;
            }
            Add_term(strb.append(from_number_to_string(f)).append(" ").append(nextTkn),position_of_word);
            index++;
            return true;
        }
        else if (is_price_bigger_then_million(f))
            return true;
        return false;
    }

    private String is_Date(float f, String nextTkn)
    {

        if(f>31 || f<1)
            return null;
        if(! month.containsKey(lowerCase(nextTkn)))
            return null;
        else
              return month.get(lowerCase(nextTkn));
    }

    private Boolean is_price_bigger_then_million(float f) {
        if(index +3 < stk.length && equalsIgnoreCase(stk[index+2],"U.S.") && equalsIgnoreCase(stk[index+3],"dollars"))
        {
            if (equalsIgnoreCase(stk[index + 1],"billion"))
            {
                Add_term(strb.append(from_number_to_string(f*1000)).append(" M Dollars "),position_of_word);
                index = index + 3;
                return true;
            }
            else if(equalsIgnoreCase(stk[index + 1],"million"))
            {
                Add_term(strb.append(from_number_to_string(f)).append(" M Dollars "),position_of_word);
                index = index + 3;
                return true;
            }
            else if(equalsIgnoreCase(stk[index + 1],"trillion"))
            {
                Add_term(strb.append(from_number_to_string(f*1000000)).append(" M Dollars "),position_of_word);
                index = index + 3;
                return true;
            }
        }
        else if (index +2 < stk.length && equalsIgnoreCase(stk[index+2],"Dollars") ){
            if(stk[index + 1].equals("m"))
            {
                Add_term(strb.append(from_number_to_string(f)).append(" M Dollars"),position_of_word);
                index = index + 2;
                return true;
            }
            else if(equalsIgnoreCase(stk[index + 1],"bn"))
            {
                Add_term(strb.append(from_number_to_string(f*1000)).append(" M Dollars"),position_of_word);
                index = index + 2;
                return true;
            }
        }
        else if(equalsIgnoreCase(stk[index+1],"Dollars"))
        {
            Add_term(strb.append(from_number_to_string(f)).append(" M Dollars"),position_of_word);
            index = index + 1;
            return true;
        }
        return false;
    }

    private boolean is_fraction(String nextTkn)
    {
        if (contains(nextTkn,"/")) {
            String[] rat = splitByWholeSeparator(nextTkn,"/");
            if(rat.length == 2 && isNumeric(rat[0]))
                return true;
        }
        return false;
    }

    private String from_number_to_string(float f)
    {
        double x = f - Math.floor(f);
        if(x == 0.)
            return (Integer.toString(((int) f)));
        else
            return String.format("%.2f", f);

    }

    public void Create_City_Map() throws IOException
    {
        URL url = new URL("https://restcountries.eu/rest/v2/all?fields=name;capital;population;currencies");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        InputStreamReader input_stem =  new InputStreamReader(con.getInputStream());

        BufferedReader in = new BufferedReader(input_stem);
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null)
        {
            content.append(inputLine);
        }
        String[] stk;
        String[] stk1;
        stk= splitByWholeSeparator(content.toString(),"{\"currencies\"");
        for (int i=0 ; i<stk.length;i++) {
            stk[i]=replace(stk[i],":","");
            stk[i]=replace(stk[i],"[","");
            stk[i]=replace(stk[i],"{","");

            stk1 = splitByWholeSeparator(stk[i],",");

            if(stk1.length>4) {
                if(stk1.length!=8) {
                    parse_info_city(stk1[stk1.length - 7 + 3],stk1[stk1.length - 3],stk1[stk1.length - 2],stk1[0]);
                }
                else {
                    if(stk1[3].charAt(0) == '}')
                        parse_info_city(stk1[4],stk1[stk1.length - 3],stk1[stk1.length-2],stk1[0]);
                    else
                        parse_info_city(stk1[3],stk1[stk1.length - 3],stk1[stk1.length-2],stk1[0]);
                }
            }
        }
        in.close();
        con.disconnect();
    }

    public void Create_City_Posting(StringBuilder sb) throws IOException
    {
        StringBuilder tmp = new StringBuilder();
            if (sb.toString().length() > 0) {
                String[] s = split(sb.toString(), " ");
                if (Capital_City.containsKey(sb.toString())) {
                    parsedDoc.setCity(sb.toString());
                    parsedDoc.setInfo_city(tmp.append(Capital_City.get(sb.toString()).toString()).toString());
                }
                else if(Capital_City.containsKey(s[0])) {
                    parsedDoc.setCity(s[0]);
                    parsedDoc.setInfo_city(tmp.append(Capital_City.get(s[0]).toString()).toString());
                }
                else if(Character.isLetter(s[0].charAt(0))&& s[0].length()>0)
                    parsedDoc.setCity(tmp.append(s[0]).toString());
            }
            sb.setLength(0);
    }

    private void parse_info_city(String country , String city,String population , String currency)
    {
        String[] stk1;
        String[] stk2;
        String[] stk3;
        String[] stk4;
        population = remove(population,'}');
        stk1 = splitByWholeSeparator(country,"\"");
        stk2 = splitByWholeSeparator(city,"\"");
        stk3 = splitByWholeSeparator(population,"\"");
        stk4 = splitByWholeSeparator(currency,"\"");

        if(stk2[1].length() == 0)
            if(stk1[1].equals("United States of America"))
                stk2[1] = "Washington D.C.";
            else
                stk2[1] = "(None)";

        if(stk4[1].equals("(none)") || stk4[1].equals("D]"))
            stk4[1] = "USD";

        if(stk2[1].equals("Zimbabwe"))
        {
            stk1[1]= "Zimbabwe";
            stk2[1]="Harare";
            stk3[1]="14240168";
            stk4[1]="GBP";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(";").append(stk1[1]).append(";").append(stk4[1]).append(";").append(cheak_size_pupolation(stk3[1]));
        Capital_City.put(stk2[1],sb);
    }

    public String cheak_size_pupolation(String sb)
    {
       float f = Float.parseFloat(sb);
            if (f < 1000)
               return is_under_10(from_number_to_string(f));
            else if (f < 1000000 && f >= 1000) {
                f = f / 1000;
                return from_number_to_string(f) + "k";
            }
            else if (f < 1000000000 && f >= 1000000) {
                f = f / 1000000;
                return from_number_to_string(f) + "M";
            }
                 else {
                f = f / 1000000000;
                return from_number_to_string(f) + "B";
            }
    }

    public void resetParse()
    {
        month.clear();
        stop_words.clear();
        tmp_word.clear();
        Capital_City.clear();
        stemmer =null ;
    }
}
