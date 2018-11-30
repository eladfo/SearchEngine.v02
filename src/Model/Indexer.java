package Model;

import java.io.*;
import java.util.*;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static org.apache.commons.lang3.StringUtils.*;


public class Indexer {
    public TreeMap<String, Term> tmpTermsDic;
    public TreeMap<String, StringBuilder> tmpCityDic;
    public HashSet<ParsedDoc> tmpDocsDic;
    public TreeMap<String, int[]> finalTermsDic;
    public int partitions;
    public String postingsPath;
    public Boolean isStemm;

//    String eladMainPost = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v01\\src\\main\\resources\\MergedPosting_NoStemmer";
//    String eladFinalPath = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v01\\src\\main\\resources\\";
//    String eladTmpTermPost = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v01\\src\\main\\resources\\termPosting";
//    String eladTmpDocPost = "C:\\Users\\e-pc\\IdeaProjects\\SearchEngine.v01\\src\\main\\resources\\docPosting";
//    String alon = "C:\\Users\\A\\Downloads\\corpus_cheak";

    public Indexer(String postPath, int partitions, Boolean isStemm) throws IOException {
        tmpTermsDic = new TreeMap<>();
        tmpDocsDic = new HashSet<>();
        tmpCityDic = new TreeMap<>();
        finalTermsDic = new TreeMap<>(); //the dic save prt to the first line in the posting.
        postingsPath = postPath + "\\";
        this.partitions = partitions;
        this.isStemm = isStemm;
    }

    public TreeMap<String, Term> getDic() {
        return tmpTermsDic;
    }

    public void createInvertedIndex() throws IOException {
        mergeTermsPostings();
        //mergePostings_city();
        createFinalTermsPostings();
        printFinalDic();
    }

    public void addParsedDoc(ParsedDoc pd)
    {
        tmpDocsDic.add(pd);
        StringBuilder docID = pd.getDocID();
        HashMap<String, StringBuilder> terms = pd.getTerms();
        int maxTF = 0;
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet())
        {
            String termID = entry.getKey();
            StringBuilder termPositions = entry.getValue();
            updateTmpTermsDic(termID, termPositions, docID);
            //if(value.size() > maxTF)
            //maxTF = value.size();
        }
        if(pd.getCity().toString().length()>0){
            if (tmpCityDic.containsKey(upperCase(pd.getCity().toString())))
                tmpCityDic.get(upperCase(pd.getCity().toString())).append(pd.getDocID()).append(':').append(pd.get_pos_city(upperCase(pd.getCity().toString()))).append(" "));

            else {
                if(pd.getInfo_city().toString().length()>0)
                    tmpCityDic.put(upperCase(pd.getCity().toString()), pd.getInfo_city().append("\n").append(pd.getDocID()).append(":").append(pd.get_pos_city(upperCase(pd.getCity().toString()))).append(" "));
                else
                    tmpCityDic.put(upperCase(pd.getCity().toString()),pd.getDocID().append(":").append(pd.get_pos_city(upperCase(pd.getCity().toString()))).append(" "));
            }
        }
        int max_tf = pd.getMaxTF();
        int docTermsSize = terms.size();
        StringBuilder city;
        pd.resetTerms();
    }

    public void setDic(TreeMap<String, Term> dic) {
        this.tmpTermsDic = dic;
    }

    public void updateTmpTermsDic(String termID, StringBuilder tPositions, StringBuilder docID) {
        if (tmpTermsDic.containsKey(lowerCase(termID)))
        {
            Term t = tmpTermsDic.get(lowerCase(termID));
            t.addDoc(docID.toString(), tPositions);
            tmpTermsDic.put(lowerCase(termID),t);
        } else
            {
            char c = termID.charAt(0);
            int flag = isUpperCase(c);
            Term t = new Term(docID.toString(), tPositions, flag);
            tmpTermsDic.put(lowerCase(termID), t);
        }
    }

    public void updateTmpDocsDic() {

    }

    public int isUpperCase(char c){
        if(!Character.isDigit(c) && c == toUpperCase(c))
            return 1;
        else
            return 0;
    }

    public void createTermTmpPosting(int idx) throws IOException {
        FileWriter fw = null;
        fw = new FileWriter(postingsPath + "termTmpPost" + idx);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Map.Entry<String, Term> entry : tmpTermsDic.entrySet()) {
            String termID = entry.getKey();
            Term termDocInfo = entry.getValue();
            StringBuilder sb = termDocInfo.from_term_to_string();
            if(termDocInfo.flag == 0)
                bw.write("*" + termID + "\n");
            else {
                bw.write("#" + termID + "\n");
            }
            bw.write(sb.toString());
        }
        bw.flush();
        bw.close();
    }

    public void mergeTermsPostings() throws IOException {
        FileWriter fw = new FileWriter(postingsPath + "mergedTermPost");
        BufferedWriter bw = new BufferedWriter(fw);
        BufferedReader[] brArray = new BufferedReader[partitions];
        String[] termsArray = new String[partitions];
        int flag=0;
        for(int i=0; i<partitions ; i++)
        {
            File file = new File(postingsPath + "termTmpPost" +i);
            brArray[i] = new BufferedReader(new FileReader(file));
            termsArray[i] = brArray[i].readLine();
            if(termsArray[i].charAt(0) == '#') {flag = 1;}
                else {flag = 0;}
            termsArray[i] = termsArray[i].substring(1);
        }
        int res;
        while(stopCondition(partitions, termsArray))
        {
            res = termsCompare(termsArray);
            if (!finalTermsDic.containsKey(termsArray[res])) {
                if (flag == 1 && !isDuplicate(termsArray, res))
                    termsArray[res] = upperCase(termsArray[res]);
                finalTermsDic.put(termsArray[res], null);
                bw.write("\n");
                bw.write("*" + termsArray[res] + "\n");
            }
            while ((termsArray[res] = brArray[res].readLine()) != null) {
                char c = termsArray[res].charAt(0);
                if (c == '*' || c == '#') {
                    if (c == '*') {
                        flag = 0;
                    } else {
                        flag = 1;
                    }
                    termsArray[res] = termsArray[res].substring(1);
                    break;
                } else {
                    bw.write(termsArray[res]);
                }
            }
        }
        //bw.flush();
        bw.close();
        for (int i=0 ; i<brArray.length;i++)
            brArray[i].close();
    }

    private boolean isDuplicate(String[] termsArray, int res) {
        String s = termsArray[res];
        for (int i = 0; i < termsArray.length ; i++) {
            if(i != res && termsArray[i]!= null && termsArray[i].equals(s))
                return true;
        }
        return false;
    }

    public boolean stopCondition(int partitions, String[] brArr) {
        for(int i=0; i<partitions; i++)
        {
            if(brArr[i] != null)
                return true;
        }
        return false;
    }

    public int termsCompare(String[] strArray) {
        String minValue = null;
        int index_min=0;
        for(int i=0 ; i<strArray.length ; i++) {
            if(strArray[i]!=null) {
                minValue = strArray[i];
                index_min=i;
                break;
            }
        }
        for(int i=1; i<strArray.length;i++){
            if(strArray[i]!= null && strArray[i].compareTo(minValue)< 0){
                minValue = strArray[i];
                index_min=i;
            }
        }
        return index_min;
    }

    public void createFinalTermsPostings() throws IOException {
        String[] posts = {"NUM","A","B","C","D","E","F","G","H","I","J","K","L","M",
                            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        File file = new File(postingsPath + "mergedTermPost");
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + posts[0]));
        String line = br.readLine();
        char cLine;
        int rowPtr = -1;
        while ((line = br.readLine()) != null) {
            cLine = line.charAt(1);
            if(Character.isDigit(cLine)) {
                bw.write(line + "\n");
                bw.write(updateFinalDic(br, line,rowPtr+=2) + "\n");
            } else break;
        }
        bw.flush();
        bw = new BufferedWriter(new FileWriter(postingsPath + posts[1]));
        char c = 'a';
        cLine = line.charAt(1);
        rowPtr = -1;
        for (int i = 2; i < posts.length+1; i++) {
            while(toLowerCase(cLine) == c || cLine == toUpperCase(c)){
                bw.write(line+"\n");
                bw.write(updateFinalDic(br, line,rowPtr+=2) + "\n");
                if((line = br.readLine()) == null) break;
                cLine = line.charAt(1);
            }
            bw.flush();
            c++;
            rowPtr = -1;
            if(i < posts.length)
                bw = new BufferedWriter(new FileWriter(postingsPath + posts[i]));
        }
    }

    private String updateFinalDic(BufferedReader br, String line, int ptr) throws IOException {
        int ttf=0; int df=0; int rowPtr=0;
        String termID = substring(line, 1);
        String termData = br.readLine();
        String[] docs = split(termData, "~");
        df = docs.length;
        for (String s : docs) {
            String[] tfd = split(s, ",");
            ttf += tfd.length - 1;
        }
        finalTermsDic.put(termID, new int[]{df, ttf, ptr});
        return termData;
    }

    public void resetIndex(){
        tmpTermsDic.clear();
        tmpDocsDic.clear();
        tmpCityDic.clear();
    }

    public void createTmpCityPosting(int idx) throws IOException
    {
        FileWriter fw = null;
        fw = new FileWriter(postingsPath+"City"+idx);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Map.Entry<String, StringBuilder> entry : tmpCityDic.entrySet())
        {
            String City_id = entry.getKey();
            StringBuilder sb = entry.getValue();

            bw.write("*" + City_id +  "\n");
            bw.write(sb.toString()+ "\n");
        }
    }

    public void mergePostings_city( ) throws IOException
    {
        FileWriter fw = new FileWriter(postingsPath+"City_final");
        BufferedWriter bw = new BufferedWriter(fw);
        BufferedReader[] brArray = new BufferedReader[partitions];
        String[] strArray = new String[partitions];
        String last_city = "";

        for(int i=0; i<partitions ; i++) {
            File file = new File(postingsPath+"City"+i);
            brArray[i] = new BufferedReader(new FileReader(file));
            strArray[i] = brArray[i].readLine();
            strArray[i] = strArray[i].substring(1);
        }
        int res=0;
        while(stopCondition(partitions, strArray))
        {
            res = termsCompare(strArray);
            if(strArray[res].compareTo(last_city) != 0) {
                last_city = strArray[res];
                bw.write("*" + strArray[res] + "\n");
            }
                else
                    brArray[res].readLine();
            while ((strArray[res] = brArray[res].readLine()) != null) {
                if (strArray[res].charAt(0) == '*') {
                    strArray[res] = strArray[res].substring(1);
                    break;
                } else {
                    bw.write(strArray[res] + "\n");
                }
            }
        }
        bw.flush();
        for (int i=0 ; i<brArray.length;i++)
            brArray[i].close();
    }


    public void printFinalDic() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + "FinalTermsDic_NoStemmer"));
        long p0 = System.currentTimeMillis();
        for (Map.Entry<String, int[]> entry : finalTermsDic.entrySet()) {
            String termID = entry.getKey();
            int[] termData = entry.getValue();
//            StringBuilder sb = new StringBuilder();
//            sb.append(termID).append(" ").append(termData[0]).append(" ").append(termData[1])
//                    .append(" ").append(termData[2]).append("\n");
//            bw.write(sb.toString());
            bw.write(termID + " " + termData[0] + " " + termData[1] + " " + termData[2] + "\n");
        }
        long p1 = System.currentTimeMillis();
        System.out.println("----- " + (p1 - p0));
        bw.flush();
    }
}
