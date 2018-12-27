package Model;
import java.io.*;
import java.util.*;
import static java.lang.Character.*;
import static org.apache.commons.lang3.StringUtils.*;

public class Indexer {
    public TreeMap<String, Term> tmpTermsDic;
    public TreeSet<ParsedDoc> tmpDocsDic;
    public TreeMap<String, StringBuilder> tmpCityDic;
    public TreeMap<String, int[]> finalTermsDic;
    public TreeMap<String, int[]> finalDocsDic;
    public TreeMap<String, Integer> finalCitiesDic;
    public int partitions;
    public String postingsPath;

    /**
     * Index's_Constructor
     */
    public Indexer(String postPath, int partitions) {
        tmpTermsDic = new TreeMap<>();
        tmpDocsDic = new TreeSet<>(Comparator.comparing(o -> o.getDocID().toString()));
        tmpCityDic = new TreeMap<>();
        finalTermsDic = new TreeMap<>();
        finalDocsDic = new TreeMap<>();
        finalCitiesDic = new TreeMap<>();
        this.postingsPath = postPath;
        this.partitions = partitions;
        createPostingsDir();
    }

    /**
     * Create directory which in it, the program save all the postings files and the dictionaries,
     * according to the GUI choice(with or without stemming).
     */
    private void createPostingsDir() {
        File dir;
        dir = new File(postingsPath);
        if(!dir.exists())
            dir.mkdir();
        postingsPath += "\\";
    }

    /**
     * Charge on calling all the essentials functions that create the Inverted Index.
     */
    public void createInvertedIndex() throws IOException {
        mergeTermsPostings();
        mergeCityPostings();
        tmpCityDic.clear();
        mergeDocsPostings();
        tmpDocsDic.clear();
        createFinalTermsPostings();
        createFinalTermsDic();
        deleteTmpFiles();
    }

    /**
     * Deleting all temp postings files, which we tagged with a prefix '_'.
     */
    private void deleteTmpFiles() {
        File directory = new File(substring(postingsPath, 0,postingsPath.length()-1));
        if(directory.listFiles() != null) {
            for (File f : directory.listFiles()) {
                if (f.getName().startsWith("_"))
                    f.delete();
            }
        }
    }

    /**
     * Calling 3 functions, that creating tmp posting files, each for different index.
     * Term, Doc, City indexes.
     * @param i - partition's index
     */
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

    /**
     * Loading the 3 dictionaries from the disk to the memory,
     * and storing them to the proper data structures.
     * @param path - posting path which the user chose
     */
    public void loadDics(String path) throws IOException {
        BufferedReader[] brs = new BufferedReader[3];
        String dicPath = path;
        brs[0] = new BufferedReader(new FileReader(new File(dicPath + "\\Final_Terms_Dic")));
        brs[1] = new BufferedReader(new FileReader(new File(dicPath + "\\Final_Docs_Dic")));
        brs[2] = new BufferedReader(new FileReader(new File(dicPath + "\\Final_Cities_Dic")));
        setFinalTermsDic(brs[0]);
        setFinalDocsDic(brs[1]);
        setFinalCityDic(brs[2]);
        for(BufferedReader br : brs)
            br.close();
    }

    /**
     * Inserting the terms's data, from the disc, into a dic on a memory.
     */
    private void setFinalTermsDic(BufferedReader br) throws IOException {
        String line;
        String[] s;
        while ((line = br.readLine()) != null){
            s = split(line, ";");
            finalTermsDic.put(
                    s[0],new int[] {Integer.valueOf(s[1]),Integer.valueOf(s[2]),Integer.valueOf(s[3])});
        }
    }

    /**
     * Inserting all the city and doc's data, from the disk, to their dics on the memory.
     */
    private void setFinalCityDic(BufferedReader br) throws IOException {
        String line;
        String[] s;
        while(( line = br.readLine()) != null){
            s = split(line, ";");
            finalCitiesDic.put(s[0], Integer.valueOf(s[1]));
        }
    }private void setFinalDocsDic(BufferedReader br) throws IOException {
        String line;
        String[] s;
        while ((line = br.readLine()) != null){
            s = split(line, ";");
            finalDocsDic.put(
                    s[0],new int[] {Integer.valueOf(s[1]),Integer.valueOf(s[2])});
        }
    }

    /**
     * The first Indexer's function, that received a ParsedDoc object from the parser,
     * take care of the data, send it to 3 update functions to temp dics.
     * @param pd - parsed doc.
     */
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
     Received term's data, and add it to a temp terms dic.
     Create a new Term object if the dic is not containing the term,
     else adding the docID and the positions which the term found in that docID,
     to the docs list. (which Term object have)
     */
    public void updateTmpTermsDic(String termID, StringBuilder tPositions, StringBuilder docID) {
         if (tmpTermsDic.containsKey(lowerCase(termID))) {
             Term t = tmpTermsDic.get(lowerCase(termID));
             if (t.flag == 1 && (isUpperCase(termID.charAt(0)) == 0))
                 t.flag = 0;
             t.addDoc(docID.toString(), tPositions);
         } else {
             char c = termID.charAt(0);
             int flag = isUpperCase(c);
             Term t = new Term(docID.toString(), tPositions, flag);
             tmpTermsDic.put(lowerCase(termID), t);
         }
    }

    /**
     * @return 1 if c is capital letter, else 0.
     */
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
            StringBuilder sb = termDocInfo.getTermDocList();
            if(termDocInfo.flag == 0)
                bw.write("*" + termID + "\n");
            else
                bw.write("#" + termID + "\n");
            bw.write(sb.toString());
        }
        bw.close();
    }

    /**
     * Read all the tmp term posting files, one foe each partition, and merged into a final big one.
     * Of course saved to the disk and not to the memory.
     */
    public void mergeTermsPostings() throws IOException {
        FileWriter fw = new FileWriter(postingsPath + "_mergedTermPosting");
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
            if(Character.isDigit(termsArray[res].charAt(0)))
                termsArray[res] = upperCase(termsArray[res]);
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
        bw.close();
        for (BufferedReader br : brArray)
            br.close();
    }

    /**
     * Checks the 'res' term has other term equal to him in termsArray.
     * @param termsArray - containing terms, one from each tmp term file.
     * @param res - the current biggest term by the string sort.
     * @return
     */
    private boolean isDuplicate(String[] termsArray, int res) {
        String s = termsArray[res];
        for (int i = 0; i < termsArray.length ; i++) {
            if(i != res && termsArray[i]!= null && termsArray[i].equals(s))
                return true;
        }
        return false;
    }

    /**
     * Checks if all buffer readers finished to read all tmp term files.
     * Return true if not, and false if all the buffers are done.
     */
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

    /**
     * Read from _mergedTermPosting file, and split it into 27 different final posting files.
     */
    public void createFinalTermsPostings() throws IOException {
        String[] posts = {"NUM","A","B","C","D","E","F","G","H","I","J","K","L","M",
                            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        File file = new File(postingsPath + "_mergedTermPosting");
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + posts[0]));
        String line = br.readLine();
        char cLine;
        int rowPtr = -1;
        while ((line = br.readLine()) != null) {
            cLine = line.charAt(1);
            if(Character.isDigit(cLine)) {
                bw.write(upperCase(line) + "\n");
                bw.write(updateFinalTermDic(br, upperCase(line),rowPtr+=2) + "\n");
            } else break;
        }
        bw.close();
        bw = new BufferedWriter(new FileWriter(postingsPath + posts[1]));
        char c = 'a';
        cLine = line.charAt(1);
        rowPtr = -1;
        for (int i = 2; i < posts.length+1; i++) {
            while(toLowerCase(cLine) == c || cLine == toUpperCase(c)){
                bw.write(line+"\n");
                bw.write(updateFinalTermDic(br, line,rowPtr+=2) + "\n");
                if((line = br.readLine()) == null) break;
                cLine = line.charAt(1);
            }
            bw.flush();
            c++;
            rowPtr = -1;
            int tmp = posts.length;
            if(i < posts.length) {
                bw.close();
                bw = new BufferedWriter(new FileWriter(postingsPath + posts[i]));
            }
        }
        br.close();
        bw.close();
    }

    /**
     * While creating the final post files, before writing a term to the disk,
     * getting a line, containing term plus its data which is doc list and the positions where the term was shown.
     * From the line, calculating the DF and TTF.
     * Updating the final terms dic with this data, plus the row pointer to the posting file.
     * @param br - BufferedReader reading from the merge term post
     * @param line - term + term data
     * @param ptr - row index
     * @return
     */
    private String updateFinalTermDic(BufferedReader br, String line, int ptr) throws IOException {
        int ttf=0;
        String termID = substring(line, 1);
        String termData = br.readLine();
        String[] docs = split(termData, "~");
        int df = docs.length;
        for (String s : docs) {
            String[] tfd = split(s, ",");
            ttf += tfd.length - 1;
        }
        finalTermsDic.put(termID, new int[]{df, ttf, ptr});
        return termData;
    }

    /**
     * From the final terms dic which locate on the memory, create one on the disk.
     */
    public void createFinalTermsDic() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + "Final_Terms_Dic"));
        for (Map.Entry<String, int[]> entry : finalTermsDic.entrySet()) {
            String termID = entry.getKey();
            int[] termData = entry.getValue();
            try {
                bw.write(termID + ";" + termData[0] + ";" + termData[1] + ";" + termData[2] + "\n");
            } catch (NullPointerException e)
            {
                System.out.println("bkaka");
            }
        }
        bw.close();
    }


    /**
     City_Index
     */
    private void updateTmpCityDic(ParsedDoc pd, StringBuilder docID) {
        String cityID = pd.getCity().toString();
        if(cityID.length()>0){
            if (tmpCityDic.containsKey(upperCase(cityID))) {
                StringBuilder cityData = tmpCityDic.get(upperCase(cityID));
                cityData.append(docID).append(':').append(pd.getCityPos(upperCase(cityID)).append(" "));
            } else {
                if(pd.getCityInfo().toString().length()>0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(pd.getCityInfo()).append("\n").append(docID).append(":").append(pd.getCityPos(upperCase(cityID))).append(" ");
                    tmpCityDic.put(upperCase(cityID), sb);
                }else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(docID).append(":").append(pd.getCityPos(upperCase(cityID))).append(" ");
                    tmpCityDic.put(upperCase(cityID), sb);
                }
            }
        }
    }

    public void createTmpCityPosting(int idx) throws IOException {
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
        bw.close();
    }

    /**
     * The merging process works identical to the terms's merging process,
     * ONLY difference is in this case, while we merging all the temp city files,
     * in the same time creating the final city dic to the disk.
     */
    public void mergeCityPostings( ) throws IOException {
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
        int rowIdx=1;
        BufferedWriter bwDic = new BufferedWriter(new FileWriter(postingsPath + "Final_Cities_Dic"));
        while(stopCondition(partitions, strArray))
        {
            res = termsCompare(strArray);
            if(strArray[res].compareTo(last_city) != 0) { //new city
                last_city = strArray[res];
                bw.write("*" + last_city + "\n");
                bwDic.write(last_city + ";" + rowIdx + "\n" );
                rowIdx++;
            }
                else
                    brArray[res].readLine();
            while ((strArray[res] = brArray[res].readLine()) != null) {
                if (strArray[res].charAt(0) == '*') {
                    strArray[res] = strArray[res].substring(1);
                    break;
                } else {
                    bw.write(strArray[res] + "\n");
                    rowIdx++;
                }
            }
        }
        bw.close();
        bwDic.close();
        for(BufferedReader br : brArray)
            br.close();
    }


    /**
     Creating doc tmp files, merging it to a big final one, and creating the final docs dic,
     works all the same as the process of Terms and Cities.
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
            StringBuilder stb = pd.getEntitiesAsStb();
            StringBuilder res = new StringBuilder();
            res.append(docid).append("~").append(maxTF).append("~").append(totalTerms).append("~").append(upperCase(city.toString()))
                    .append("~").append(pd.getFileID()).append("~").append(pd.docLength).append("~").append(stb).append("\n");
            bw.write(res.toString());
        }
        bw.close();
    }

    public void mergeDocsPostings() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(postingsPath + "mergedDocsPosting"));
        BufferedWriter bwDic = new BufferedWriter(new FileWriter(postingsPath + "Final_Docs_Dic"));
        int pointer = 1;
        for(int i=0; i<partitions ; i++) {
            BufferedReader br = new BufferedReader(new FileReader(new File(postingsPath + "_tmpDocPost" + i)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = split(line, "~");
                StringBuilder topFive = calcTopFive(tokens[6]);
                StringBuilder res = new StringBuilder();
                for(int j=0; j<tokens.length-2; j++)
                    res.append(tokens[j]).append("~");
                res.append(topFive);
                bw.write(res.toString() + "\n");
                bwDic.write(tokens[0] + ";" + tokens[5] + ";" + pointer + "\n");
                pointer++;
            }
            br.close();
        }
        bw.close();
        bwDic.close();
    }

    private StringBuilder calcTopFive(String line) {
        StringBuilder res = new StringBuilder();
        String[] entities = split(line, ",");
        int count = 0;
        for(int i=0; i<entities.length; i++){
            if(count==5)
                return res;
            else if(!entities[i].equals("null") && finalTermsDic.containsKey(entities[i])) {
                res.append(entities[i]).append(",").append(entities[i+1]).append(",");
                count++;
            }
            i++;
        }
        return res;
    }

//    public void createFinalDocsDic(BufferedWriter bwDic, String[] tokens, int pointer) throws IOException {
//        String[] tokens = split(line, "~");
//        bwDic.write(tokens[0] + ";" + pointer + "\n");
//    }
}
