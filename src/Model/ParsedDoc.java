package Model;
import java.util.HashMap;
import java.util.Map;
import static org.apache.commons.lang3.StringUtils.*;

public class ParsedDoc {
    private StringBuilder docID = new StringBuilder();
    private HashMap<String,StringBuilder> terms;
    private int maxTF;
    private int numOfTerms;
    public int docLength;
    private StringBuilder cityID;
    private StringBuilder cityInfo;
    private String fileID;

    /**
     * Constructor
     */
    public ParsedDoc() {
        terms = new HashMap<>();
        cityID = new StringBuilder();
        cityInfo =new StringBuilder();
    }

    /**
     * Add a new term to a temporary HashMap of terms.
     * If term already exist, append the new position to the old ones.
     * @param str - parsed term
     * @param position - term position in a doc
     */
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
        }
        else {
            StringBuilder sbTmp = new StringBuilder(String.valueOf(position));
            terms.put(tmp, sbTmp);
        }
    }

    public void resetTerms() {
        terms.clear();
    }

    /**
     * Calculate a doc maxTF, and assigning it to maxTF field.
     */
    public void calcMaxTF(){
        maxTF = 0;
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet()) {
            StringBuilder termPositions = entry.getValue();
            String[] pos = split(termPositions.toString(), ",");
            if(pos.length > maxTF)
                maxTF = pos.length;
        }
    }

    /**
     * @param s - cityID
     * @return Return a cityID's position. If the city did not appeared in the doc,
     *           except for the <F P=104> tag, return position 0.
     */
    public StringBuilder getCityPos(String s) {
        StringBuilder res =  terms.get(s);
        if(res == null)
            return (new StringBuilder("0"));
        else
            return res;
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

    public StringBuilder getCityInfo() {
        return cityInfo;
    }

    public void setCityInfo(String cityInfo) {
        this.cityInfo.append(cityInfo);
    }

    public int getNumOfTerms() {
        return numOfTerms;
    }

    public void setNumOfTerms(int numOfTerms) {
        this.numOfTerms = numOfTerms;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }
}
