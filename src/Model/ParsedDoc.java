package Model;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;

public class ParsedDoc {
    private StringBuilder docID = new StringBuilder();
    private HashMap<String,StringBuilder> terms;
    private int maxTF;
    private int numOfTerms;
    private StringBuilder docCity;
    private StringBuilder info_city;

    public ParsedDoc() {
        terms = new HashMap<>();
        docCity = new StringBuilder();
        info_city=new StringBuilder();
    }



    public void addTerm(String str, int position){
        String tmp;

        if( (str.charAt(0) <= '0' || str.charAt(0) >= '9') && str.charAt(0) == Character.toUpperCase(str.charAt(0)) )
            tmp = upperCase(str);
        else
            tmp = lowerCase(str);

        if (terms.containsKey(tmp))
        {
            StringBuilder t = terms.get(tmp);
            t.append(",").append(String.valueOf(position));
            terms.put(tmp,t);
            //terms.get(tmp).append(",").append(String.valueOf(position));
        }
        else {
            StringBuilder sbTmp = new StringBuilder(String.valueOf(position));
            terms.put(tmp, sbTmp);
        }

    }

    public void resetTerms() {
        terms.clear();
    }

    public int getMaxTF() {
        maxTF = 0;
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet()) {
            StringBuilder termPositions = entry.getValue();
            String[] pos = split(termPositions.toString(), ",");
            if(pos.length > maxTF)
                maxTF = pos.length;
        }
        return maxTF;
    }

    public void setMaxTF(int maxTF) {
        this.maxTF = maxTF;
    }

    public StringBuilder getDocID() {
        return docID;
    }

    public void setDocID(StringBuilder docID) {
        this.docID = docID;
    }

    public HashMap<String,StringBuilder> getTerms() {
        return terms;
    }

    public void setTerms(HashMap<String, StringBuilder> terms) {
        this.terms = terms;
    }

    public StringBuilder getCity() {
        return docCity;
    }

    public void setCity(String city) {
        this.docCity.append(city);
    }

    public StringBuilder getInfo_city() {
        return info_city;
    }

    public void setInfo_city(String info_city) {
        this.info_city.append(info_city);
    }

    public int getNumOfTerms() {
        return numOfTerms;
    }
    public void setNumOfTerms(int numOfTerms) {
        this.numOfTerms = numOfTerms;
    }

    public StringBuilder get_pos_city(String s)
    {
        return terms.get(s);
    }
}
