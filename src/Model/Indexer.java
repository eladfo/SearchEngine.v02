package Model;

import java.io.*;
import java.util.*;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static org.apache.commons.lang3.StringUtils.*;

public class Indexer {
    public TreeMap<String, Term> tmpTermsDic;
    public TreeSet<ParsedDoc> tmpDocsDic;
    public TreeMap<String, StringBuilder> tmpCityDic;
    public TreeMap<String, int[]> finalTermsDic;
    public int partitions;
    public String postingsPath;
    public Boolean isStemm;

    /**
     * Index's_Constructor
     */
    public Indexer(String postPath, int partitions, Boolean isStemm) throws IOException {
        tmpTermsDic = new TreeMap<>();
        tmpDocsDic = new TreeSet<>(new Comparator<ParsedDoc>() {
            @Override
            public int compare(ParsedDoc o1, ParsedDoc o2) {
                return o1.getDocID().toString().compareTo(o2.getDocID().toString());
            }
        });
        tmpCityDic = new TreeMap<>();
        finalTermsDic = new TreeMap<>();
        this.postingsPath = postPath;
        this.partitions = partitions;
        this.isStemm = isStemm;
        createPostingsDir();
    }

    private void createPostingsDir() {
        File dir;
        if(isStemm)
            postingsPath += "\\With_Stemmer";
        else
            postingsPath += "\\Without_Stemmer";

        dir = new File(postingsPath);
        if(!dir.exists())
            dir.mkdir();
        postingsPath += "\\";
    }

    public void createInvertedIndex() throws IOException {
        mergeTermsPostings();
        mergePostings_city();
        mergeDocsPostings();
        createFinalTermsPostings();
        createFinalTermsDic();
        deleteTmpFiles(1);
    }

    public void deleteTmpFiles(int flag)
    {
        // "1" -- remove only the temp files
        // "0" -- remove all the files

        File directory = new File(substring(postingsPath, 0,postingsPath.length()-1));
        if(directory.listFiles() != null) {
            for (File f : directory.listFiles()) {
                if (f.getName().startsWith("_") && flag==1)
                    f.delete();
                else if(flag== 0)
                    f.delete();
            }
        }
    }

    public void createTmpPosting(int i) throws IOException {
        createTmpTermPosting(i);
        createTmpDocPosting(i);
        createTmpCityPosting(i);
    }

    public void resetIndex(){
        tmpTermsDic.clear();
        tmpDocsDic.clear();
        tmpCityDic.clear();
    }

    public void addParsedDoc(ParsedDoc pd) {
        tmpDocsDic.add(pd);
        StringBuilder docID = pd.getDocID();
        HashMap<String, StringBuilder> terms = pd.getTerms();
        for (Map.Entry<String, StringBuilder> entry : terms.entrySet()) {
            String termID = entry.getKey();
            StringBuilder termPositions = entry.getValue();
            updateTmpTermsDic(termID, termPositions, docID);
        }

        updateTmpDocsDic(pd);
        updateTmpCityDic(pd, docID);
        pd.resetTerms();
    }


    /**
     Terms_Index
     */
    public void updateTmpTermsDic(String termID, StringBuilder tPositions, StringBuilder docID) {
         if (tmpTermsDic.containsKey(lowerCase(termID))) {
             Term t = tmpTermsDic.get(lowerCase(termID));
             if (t.flag == 1 && (isUpperCase(termID.charAt(0)) == 0))
                 t.flag = 0;
             t.addDoc(docID.toString(), tPositions);
//                t.docList.put(docID.toString(), tPositions);
         } else {
             char c = termID.charAt(0);
             int flag = isUpperCase(c);
             Term t = new Term(docID.toString(), tPositions, flag);
             tmpTermsDic.put(lowerCase(termID), t);
         }
    }

    public int isUpperCase(char c){
        if(!Character.isDigit(c) && c == toUpperCase(c))
            return 1;
        else
            return 0;
    }

    public void createTmpTermPosting(int idx) throws IOException {
        FileWriter fw = new FileWriter(postingsPath + "_tmpTermPost" + idx);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Map.Entry<String, Term> entry : tmpTermsDic.entrySet()) {
            String termID = entry.getKey();
            Term termDocInfo = entry.getValue();
            StringBuilder sb = termDocInfo.from_term_to_string();
            if(termDocInfo.flag == 0)
                bw.write("*" + termID + "\n");
            else
                bw.write("#" + termID + "\n");
            bw.write(sb.toString());
        }
        bw.flush();
    }

    public void mergeTermsPostings() throws IOException {
        FileWriter fw = new FileWriter(postingsPath + "mergedTermPosting");
        BufferedWriter bw = new BufferedWriter(fw);
        BufferedReader[] brArray = new BufferedReader[partitions];
        String[] termsArray = new String[partitions];
        int flag=0;
        for(int i=0; i<partitions ; i++)
        {
            File file = new File(postingsPath + "_tmpTermPost" +i);
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
        bw.flush();
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

    private boolean stopCondition(int partitions, String[] brArr) {
        for(int i=0; i<partitions; i++)
        {
            if(brArr[i] != null)
                return true;
        }
        return false;
    }

    private int termsCompare(String[] strArray) {
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
        File file = new File(postingsPath + "mergedTermPosting");
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

    public void createFinalTermsDic() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + "Final_Terms_Dic"));
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
//        System.out.println("----- " + (p1 - p0));
        bw.flush();
    }

    /**
     City_Index
     */
    private void updateTmpCityDic(ParsedDoc pd, StringBuilder docID) {
        String cityID = pd.getCity().toString();
        if(cityID.length()>0){
            if (tmpCityDic.containsKey(upperCase(cityID))) {
                StringBuilder cityData = tmpCityDic.get(upperCase(cityID));
                cityData.append(docID).append(':').append(pd.get_pos_city(upperCase(cityID)).append(" "));
            } else {
                if(pd.getInfo_city().toString().length()>0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(pd.getInfo_city()).append("\n").append(docID).append(":").append(pd.get_pos_city(upperCase(cityID))).append(" ");
                    tmpCityDic.put(upperCase(cityID), sb);
                }else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(docID).append(":").append(pd.get_pos_city(upperCase(cityID))).append(" ");
                    tmpCityDic.put(upperCase(cityID), sb);
                }
            }
        }
    }

    public void createTmpCityPosting(int idx) throws IOException
    {
        FileWriter fw = null;
        fw = new FileWriter(postingsPath+"_tmpCityPost"+idx);
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
        FileWriter fw = new FileWriter(postingsPath+"mergedCityPosting");
        BufferedWriter bw = new BufferedWriter(fw);
        BufferedReader[] brArray = new BufferedReader[partitions];
        String[] strArray = new String[partitions];
        String last_city = "";

        for(int i=0; i<partitions ; i++)
        {
            File file = new File(postingsPath+"_tmpCityPost"+i);
            brArray[i] = new BufferedReader(new FileReader(file));
            strArray[i] = brArray[i].readLine();
            if(strArray[i] != null)
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
    }

    public void createFinalCityDic() throws IOException {

    }

    /**
     Docs_Index
     */
    public void updateTmpDocsDic(ParsedDoc pd) {
        HashMap<String, StringBuilder> terms = pd.getTerms();
        pd.calcMaxTF();
        pd.setNumOfTerms(terms.size());
    }

    public void createTmpDocPosting(int idx) throws IOException {
        FileWriter fw = new FileWriter(postingsPath + "_tmpDocPost" + idx);
        BufferedWriter bw = new BufferedWriter(fw);
        for (ParsedDoc pd : tmpDocsDic) {
            int maxTF = pd.getMaxTF();
            StringBuilder city = pd.getCity();
            int totalTerms = pd.getNumOfTerms();
            StringBuilder docid = pd.getDocID();
            bw.write(docid.toString()+"~"+maxTF+"~"+totalTerms+"~"+upperCase(city.toString())+"\n");
        }
        bw.flush();
    }

    public void mergeDocsPostings() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + "mergedDocsPosting"));
        BufferedWriter bwDic = new BufferedWriter(new FileWriter(postingsPath + "Final_Docs_Dic"));
        int pointer = 0;
        for(int i=0; i<partitions ; i++) {
            BufferedReader br = new BufferedReader(new FileReader(new File(postingsPath + "_tmpDocPost" + i)));
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line + "\n");
                createFinalDocsDic(bwDic, line ,pointer);
                pointer++;
            }
            br.close();
        }
        bw.flush();
        bwDic.flush();
    }

    public void createFinalDocsDic(BufferedWriter bwDic, String line, int pointer) throws IOException {
        String[] tokens = split(line, "~");
        bwDic.write(tokens[0] + " " + pointer + "\n");
    }
}
