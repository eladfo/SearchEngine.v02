package Model;
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
    private boolean isStem;
    private ParsedDoc parsedDoc = new ParsedDoc();
    private StringBuilder strb = new StringBuilder();
    private HashMap<String,String> monthInfo = new HashMap<>() ;
    private HashSet<String> stopWords = new HashSet<>();
    private HashSet<String> suffixWords = new HashSet<>();
    public int wordPosition;
    private int rowCounter;
    public TreeMap<String, StringBuilder> capitalCityAPI;
    public Stemmer stemmer;

    /**
     * Constructor
     */
    public Parse(String corpusPath, Boolean stemm , String stopWordPath) throws IOException {
        isStem = stemm;
        setMonthMap();
        setSuffixWords();
        if(corpusPath != "") {
            BufferedReader br = new BufferedReader(new FileReader(stopWordPath));
            String st;
            while ((st = br.readLine()) != null) {
                if (!st.isEmpty())
                    stopWords.add(lowerCase(initialParse(st)));
            }
        }
        capitalCityAPI = new TreeMap<>();
//        createCityMap();
        stemmer = new Stemmer();
    }

    /**
     * Initialize suffix words's HshSet
     */
    private void setSuffixWords() {
        suffixWords.add("thousand");
        suffixWords.add("million");
        suffixWords.add("trillion");
        suffixWords.add("billion");
        suffixWords.add("percent");
        suffixWords.add("percentage");
        suffixWords.add("dollars");
        suffixWords.add("m");
        suffixWords.add("bn");
        suffixWords.add("billion");
        suffixWords.add("million");
        suffixWords.add("trillion");
    }

    /**
     * Initialize month info HashMap
     */
    private void setMonthMap() {
        monthInfo.put("january","01");
        monthInfo.put("february","02");
        monthInfo.put("march","03");
        monthInfo.put("april","04");
        monthInfo.put("may","05");
        monthInfo.put("june","06");
        monthInfo.put("july","07");
        monthInfo.put("august","08");
        monthInfo.put("september","09");
        monthInfo.put("october","10");
        monthInfo.put("november","11");
        monthInfo.put("december","12");
    }

    public void addTermToParsedDoc(StringBuilder sb, int pos) {
        if(sb.toString().charAt(0) == '%')
            return;
        if(isStem)
            parsedDoc.addTerm(stemmer.stem(sb.toString()), pos);
        else
            parsedDoc.addTerm(sb.toString(), pos);
        wordPosition++;
    }

    /**
     * Received object with original text,
     * @return  return object with parsed words.
     */
    public ParsedDoc runParser(Doc doc , boolean with_parse) {
        isStem = with_parse;
        parsedDoc = new ParsedDoc();
        parsedDoc.setDocID(doc.getDocID());
        char x = '"';
        stk = split(doc.getDocText().toString(), x + " `'_*&#+<>|~\\,;][:^@()?{}!�");

        parse_arr_token(1);

        parsedDoc.docLength = rowCounter;    //Update the row

        parse_header(doc);

        updateCityInfo(doc.getDocCity());
        parsedDoc.setFileID(doc.getDocFile());
        return parsedDoc;
    }

    private void parse_header(Doc doc)
    {
        char x = '"';
        stk = split(doc.getDocHeader().toString(), x + " `'_*&#+<>|~\\,;][:^@()?{}!�");
        parse_arr_token(0);
    }

    private void parse_arr_token(int flag)
    {
        rowCounter = 0;
        wordPosition = 1;
        while (rowCounter < stk.length) {
            String s = initialParse(stk[rowCounter]);
            if (s.length() == 0 || stopWords.contains(lowerCase(s))) {
                rowCounter++;
                continue;
            }
            strb.setLength(0);

            if (s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                if (s.charAt(s.length() - 1) == '%') {
                    addTermToParsedDoc(strb.append(s), flag*wordPosition);
                } else if (!isFraction(s)) {
                    tokenIsNum(s , flag);
                } else if (rowCounter + 1 < stk.length && equalsIgnoreCase(stk[rowCounter + 1], "Dollars")) {
                    addTermToParsedDoc(strb.append(s).append(" Dollars"), flag*wordPosition);
                }
            } else if (Character.isLetter(s.charAt(0)))    //word!!
            {
                if (rowCounter + 1 < stk.length && monthInfo.containsKey(lowerCase(s)) && isNumeric(stk[rowCounter + 1])) {
                    addTermToParsedDoc(strb.append(monthInfo.get(lowerCase(s))).append("-")
                            .append(isNumUnder10(initialParse(stk[rowCounter + 1]))), flag*wordPosition);
                    rowCounter++;
                    continue;
                } else {
                    if (contains(s, "--")) {
                        strb.setLength(0);
                        String[] tmp = split(s, "--");
                        for (int j = 0; j < tmp.length; j++)
                            if (stopWords.contains(lowerCase(tmp[j])))
                                continue;
                        for (int j = 0; j < tmp.length - 1; j++) {
                            if (stopWords.contains(lowerCase(tmp[j])))
                                continue;
                            else {
                                tmp[j] = replace(tmp[j], "$", "");
                                tmp[j] = initialParse(tmp[j]);
                                strb.append(tmp[j]);
                            }
                        }
                    } else if (contains(s, "-")) {
                        strb.setLength(0);
                        String[] tmp = split(s, "-");
                        if (tmp.length != 1) {
                            addTermToParsedDoc(strb.append(s), flag*wordPosition);
                            strb.setLength(0);
                        }
                        for (int j = 0; j < tmp.length; j++) {
                            tmp[j] = initialParse(tmp[j]);
                            if (!stopWords.contains(tmp[j])) {
                                if (s.contains("/") || s.contains("."))
                                    clean(tmp[j],flag*wordPosition,flag);
                                else
                                    addTermToParsedDoc(strb.append(tmp[j]),  flag*wordPosition);
                            }
                            strb.setLength(0);
                        }

                    } else {     //regular worddd
                        if (s.contains("/") || s.contains("."))
                            clean(s, flag*wordPosition,flag);
                        else
                            addTermToParsedDoc(strb.append(s),flag* wordPosition);
                        strb.setLength(0);
                    }
                }
            }
            strb.setLength(0);
            rowCounter++;
        }
    }

    private void clean(String s , int pos , int flag)
    {
        if(s.equals("U.S."))
        {
            addTermToParsedDoc(strb.append(s), flag*pos);
            strb.setLength(0);
            return;
        }
        String[] tokens = split(s,"./");
        for(String word : tokens)
        {
            if(word.contains("/") || word.contains("."))
                clean(word, pos,flag);
            else{
                addTermToParsedDoc(strb.append(word), flag*pos);
                strb.setLength(0);
            }
        }
    }

    /**
     * Received numbers smaller then 10, and link 0 to their beginning.
     */
    private String isNumUnder10(String s) {
        if( !isTokenFloat(s)&& Integer.parseInt(s)<10)
            return ("0"+s) ;
        return s;
    }

    /**
     * Initial Parsing.
     * @param s - term's token
     * @return term without special symbols
     */
    private String initialParse(String s) {
        char tmp = '"';
        //s= replaceChars(s,tmp,'#');
        //s=replaceChars(s,"'#+<>|~,!�","");
        if (org.apache.commons.lang3.StringUtils.contains(s,".") && !s.equals("U.S.")
                && !Character.isDigit(s.charAt(0)) && s.charAt(0)!='$')
            s=  replaceChars(s,".","");
        else if (s.length()>0 && s.charAt(s.length()-1)  == '.' && !s.equals("U.S.") )
            s=replaceChars(s,".","");
        return s;
    }

    private void tokenIsNum(String s , int flag_header) {
        if(contains(s,"-")) {
            addTermToParsedDoc(strb.append(s), flag_header*wordPosition);
            return;
        }
        float f;
        if(isNumeric(s)|| isTokenFloat(s))
            f = Float.parseFloat(s);
        else
        {
            return;
        }
        Boolean flag = false;
        if (rowCounter + 1 < stk.length && suffixWords.contains(lowerCase(stk[rowCounter + 1]))) {
            String nextTkn = stk[rowCounter + 1];
            flag = checkNextTkn(f, nextTkn , flag_header);
        }
        if (!flag) {
            if (f < 1000) {
                addTermToParsedDoc(strb.append(isNumUnder10(fromNumToString(f))), flag_header*wordPosition);
            } else if (f < 1000000 && f >= 1000) {
                f = f / 1000;
                addTermToParsedDoc(strb.append(fromNumToString(f)).append("K"), flag_header*wordPosition);
            } else if (f < 1000000000 && f >= 1000000) {
                f = f / 1000000;
                addTermToParsedDoc(strb.append(fromNumToString(f)).append("M"), flag_header*wordPosition);
            } else if (f >= 1000000000) {
                f = f / 1000000000;
                addTermToParsedDoc(strb.append(fromNumToString(f)).append("B"), flag_header*wordPosition);
            }
        }
    }

    private boolean isTokenFloat(String s) {
        try {
            float f = Float.parseFloat(s);
            return true;
        }catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks is there is a connection between a float number to the word after it.
     * @param f - float Token
     * @param nextTkn - after f
     * @return True if succeed to find and combine them together. else False.
     */
    private Boolean checkNextTkn(float f, String nextTkn , int flag_header) {

        if (equalsIgnoreCase(nextTkn,"Thousand")) {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append("K"), flag_header*wordPosition);
            rowCounter++;
            return true;
        }
        else if (isNumDate(f,nextTkn) != null) {
            addTermToParsedDoc(strb.append(isNumDate(f,nextTkn)).append("-").append(fromNumToString(f)), flag_header*wordPosition);
            rowCounter++;
            return true;
        }else if (equalsIgnoreCase(nextTkn,"Million")) {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append("M"),flag_header*wordPosition);
            rowCounter++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"Billion")) {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append("B"), flag_header*wordPosition);
            rowCounter++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"Trillion")) {
            addTermToParsedDoc(strb.append(fromNumToString(f*1000)).append("B"), flag_header*wordPosition);
            rowCounter++;
            return true;
        } else if (equalsIgnoreCase(nextTkn,"percent") || nextTkn.equals("percentage")) {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append("%"), flag_header*wordPosition);
            rowCounter++;
            return true;
        }
        else if (f < 1000000 && equalsIgnoreCase(nextTkn,"Dollars")) {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append(" ").append(nextTkn), flag_header*wordPosition);
            rowCounter++;
            return true;
        }
        else if (isFraction(nextTkn)) {
            if (rowCounter + 2 < stk.length && stk[rowCounter + 2].equals("Dollars")) {
                addTermToParsedDoc(strb.append(fromNumToString(f)).append(" ").append(nextTkn).append("Dollars"), flag_header*wordPosition);
                rowCounter = rowCounter + 2;
                return true;
            }
            addTermToParsedDoc(strb.append(fromNumToString(f)).append(" ").append(nextTkn), flag_header*wordPosition);
            rowCounter++;
            return true;
        }
        else if (isPriceBiggerThenMillion(f ,flag_header))
            return true;
        return false;
    }

    private String isNumDate(float f, String nextTkn) {
        if(f>31 || f<1)
            return null;
        if(! monthInfo.containsKey(lowerCase(nextTkn)))
            return null;
        else
            return monthInfo.get(lowerCase(nextTkn));
    }

    private Boolean isPriceBiggerThenMillion(float f , int flag_header) {
        if(rowCounter +3 < stk.length && equalsIgnoreCase(stk[rowCounter +2],"U.S.") && equalsIgnoreCase(stk[rowCounter +3],"dollars"))
        {
            if (equalsIgnoreCase(stk[rowCounter + 1],"billion"))
            {
                addTermToParsedDoc(strb.append(fromNumToString(f*1000)).append(" M Dollars "), flag_header*wordPosition);
                rowCounter = rowCounter + 3;
                return true;
            }
            else if(equalsIgnoreCase(stk[rowCounter + 1],"million"))
            {
                addTermToParsedDoc(strb.append(fromNumToString(f)).append(" M Dollars "), flag_header*wordPosition);
                rowCounter = rowCounter + 3;
                return true;
            }
            else if(equalsIgnoreCase(stk[rowCounter + 1],"trillion"))
            {
                addTermToParsedDoc(strb.append(fromNumToString(f*1000000)).append(" M Dollars "), flag_header*wordPosition);
                rowCounter = rowCounter + 3;
                return true;
            }
        }
        else if (rowCounter +2 < stk.length && equalsIgnoreCase(stk[rowCounter +2],"Dollars") ){
            if(stk[rowCounter + 1].equals("m"))
            {
                addTermToParsedDoc(strb.append(fromNumToString(f)).append(" M Dollars"), flag_header*wordPosition);
                rowCounter = rowCounter + 2;
                return true;
            }
            else if(equalsIgnoreCase(stk[rowCounter + 1],"bn"))
            {
                addTermToParsedDoc(strb.append(fromNumToString(f*1000)).append(" M Dollars"), flag_header*wordPosition);
                rowCounter = rowCounter + 2;
                return true;
            }
        }
        else if(equalsIgnoreCase(stk[rowCounter +1],"Dollars"))
        {
            addTermToParsedDoc(strb.append(fromNumToString(f)).append(" M Dollars"), flag_header*wordPosition);
            rowCounter = rowCounter + 1;
            return true;
        }
        return false;
    }

    /**
     * Checks if token is fraction number.
     */
    private boolean isFraction(String nextTkn) {
        if (contains(nextTkn,"/")) {
            String[] rat = splitByWholeSeparator(nextTkn,"/");
            if(rat.length == 2 && isNumeric(rat[0]))
                return true;
        }
        return false;
    }

    private String fromNumToString(float f) {
        double x = f - Math.floor(f);
        if(x == 0.)
            return (Integer.toString(((int) f)));
        else
            return String.format("%.2f", f);
    }

    /**
     * Loading the whole city's data from an external API.
     */
    public void createCityMap() throws IOException {
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
        cityDataToLines(content);
        in.close();
        con.disconnect();
    }

    /**
     * Parse and Disassemble the city's info into lines - Currency, Country, Population size,
     * and send it to the main City's info parser.
     * @param content - City's Info from API
     */
    public void cityDataToLines(StringBuffer content){
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
                    parseCityInfo(stk1[stk1.length - 7 + 3],stk1[stk1.length - 3],stk1[stk1.length - 2],stk1[0]);
                }
                else {
                    if(stk1[3].charAt(0) == '}')
                        parseCityInfo(stk1[4],stk1[stk1.length - 3],stk1[stk1.length-2],stk1[0]);
                    else
                        parseCityInfo(stk1[3],stk1[stk1.length - 3],stk1[stk1.length-2],stk1[0]);
                }
            }
        }
    }

    /**
     * Create a StringBuilder that contain the final city's data(city, country, currency, population)
     * and enter it to capitalCityAPI.
     */
    private void parseCityInfo(String country , String city, String population , String currency) {
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
        sb.append(";").append(stk1[1]).append(";").append(stk4[1]).append(";").append(convertPopulationSize(stk3[1]));
        capitalCityAPI.put(stk2[1],sb);
    }

    /**
     * Link K,M,B to the correct number.
     * @param sb - Population size
     */
    public String convertPopulationSize(String sb) {
        float f = Float.parseFloat(sb);
        if (f < 1000)
            return isNumUnder10(fromNumToString(f));
        else if (f < 1000000 && f >= 1000) {
            f = f / 1000;
            return fromNumToString(f) + "K";
        }
        else if (f < 1000000000 && f >= 1000000) {
            f = f / 1000000;
            return fromNumToString(f) + "M";
        }
        else {
            f = f / 1000000000;
            return fromNumToString(f) + "B";
        }
    }

    /**
     * Update the parsedDoc with the data from capitalCityAPI, about the city.
     * @param sb - whole line of <F P=104>
     * @throws IOException
     */
    public void updateCityInfo(StringBuilder sb){
        StringBuilder tmp = new StringBuilder();
        if (sb.toString().length() > 0) {
            String[] s = split(sb.toString(), " ");
            if (capitalCityAPI.containsKey(sb.toString())) {
                parsedDoc.setCity(sb.toString());
                parsedDoc.setCityInfo(tmp.append(capitalCityAPI.get(sb.toString()).toString()).toString());
            }
            else if(capitalCityAPI.containsKey(s[0])) {
                parsedDoc.setCity(s[0]);
                parsedDoc.setCityInfo(tmp.append(capitalCityAPI.get(s[0]).toString()).toString());
            }
            else if(Character.isLetter(s[0].charAt(0))&& s[0].length()>0)
                parsedDoc.setCity(tmp.append(s[0]).toString());
        }
        sb.setLength(0);
    }

    /**
     * Resetting all data structures of object Parse.
     */
    public void resetParse() {
        monthInfo.clear();
        stopWords.clear();
        suffixWords.clear();
        capitalCityAPI.clear();
        stemmer =null ;
    }
}
