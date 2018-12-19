package Model;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Character.isUpperCase;
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
    public TreeMap<String, Double> entities;

    /**
     * Constructor
     */
    public ParsedDoc() {
        terms = new HashMap<>();
        cityID = new StringBuilder(" ");
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
        if(str.length()==0)
            return;
        char c = str.charAt(0);
        if( (c <= '0' || c >= '9') && isUpperCase(c) )
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
        entities = new TreeMap<>();
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet()) {
            StringBuilder termPositions = entry.getValue();
            String[] pos = split(termPositions.toString(), ",");
            if(pos.length > maxTF)
                maxTF = pos.length;
            if(isUpperCase(entry.getKey().charAt(0))){
                double finalEntityRank = calcEntityFinalRank(pos, 0.7, 0.3);
                entities.put(entry.getKey(), finalEntityRank);
            }
        }
    }

    /**
     * calculate entity's final rank by the formula of (alpha*df + beta*positionRank)
     * @param alpha - df's factor
     * @param beta - position formula's factor.
     * @return final entity rank by our formula.
     */
    public double calcEntityFinalRank(String[] pos, double alpha, double beta){
        int posRank = (docLength - Integer.valueOf(pos[0])) / docLength;
        double finalRank = (int) (alpha*pos.length + beta*posRank);
        return finalRank;
    }

    /**
     * calculate top 5 Entities per doc, and return them.
     */
    public String[] calcTop15Entity() {
        String[] topFifteen = new String[15];
        int idx = 0;
        for (Map.Entry<String, Double> entry : entities.entrySet()) {
            if(idx == 15)
                return topFifteen;
            topFifteen[idx] = entry.getKey();
            idx++;
        }
        return topFifteen;
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
