package Model;

import java.util.HashMap;
import java.util.Map;
import static org.apache.commons.lang3.StringUtils.*;

public class ParsedDoc {
    private StringBuilder docID = new StringBuilder();
    private HashMap<String,StringBuilder> terms;
    private int maxTF;
    private int numOfTerms;
    private StringBuilder cityID;
    private StringBuilder info_city;
    public String fileID;

    public ParsedDoc() {
        terms = new HashMap<>();
        cityID = new StringBuilder();
        info_city=new StringBuilder();
    }

    public void addTerm(String str, int position){
        String tmp;
        char c = str.charAt(0);

        if( (c <= '0' || c >= '9') && c == Character.toUpperCase(c) )
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

    public void calcMaxTF(){
        maxTF = 0;
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet()) {
            StringBuilder termPositions = entry.getValue();
            String[] pos = split(termPositions.toString(), ",");
            if(pos.length > maxTF)
                maxTF = pos.length;
        }
    }

    public int getMaxTF() { return maxTF; }

    public StringBuilder getDocID() {
        return docID;
    }

    public void setDocID(StringBuilder docID) {
        this.docID = docID;
    }

    public HashMap<String,StringBuilder> getTerms() {
        return terms;
    }

    public StringBuilder getCity() {
        return cityID;
    }

    public void setCity(String city) {
        this.cityID.append(city);
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
        StringBuilder res =  terms.get(s);
        if(res == null)
            return (new StringBuilder("0"));
        else
            return res;
    }
}
