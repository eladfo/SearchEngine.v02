package Model;
import java.io.*;
import java.util.HashSet;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.split;

public class ReadFile {
    private File folder;
    private File[] listOfFiles;
    private Doc doc;
    private HashSet<Doc> docSet;
    private int docFlag;
    private int chunkSize;
    private String fileID;

    public ReadFile(String corpusPath) {
        folder = new File(corpusPath);
        listOfFiles = folder.listFiles();
        docSet = new HashSet<>();
        doc=null ;
        docFlag = 0;
        chunkSize = 50;
    }

    /**
     * Read 'chunkSize' number of files, divide the file data into docs and specified groups in Doc object.
     * @param extIdx - chunkSize's index.
     * @return Doc's set.
     */
    public HashSet<Doc> readLines(int extIdx) throws IOException {
            for (int i = extIdx* chunkSize; i<listOfFiles.length && i< chunkSize *(extIdx+1); i++){
                doc = new Doc();
                File innerFolder = new File(listOfFiles[i].getPath());
                File[] innerFiles = innerFolder.listFiles();
                File file = new File(listOfFiles[i] + "\\" + innerFiles[0].getName());
                fileID = innerFiles[0].getName();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String st;
                while ((st = br.readLine()) != null)
                    if (!st.isEmpty()) {
                        stringBuild(st);
                    }
            }
        return docSet;
    }

    /**
     * Insert a line, into the right group of the Doc object.
     * When reached to an end of a doc, enter it to the docSet and create a new Doc object.
     * @param st - A line from file
     */
    public void stringBuild(String st) {
        if(docFlag == 4) {
            if (st.equals("</TEXT>")) {
                docFlag = 0;
                return;
            }
            if(st.charAt(0)=='<' || contains(st, "Article Type"))
                return;
            doc.update(st, docFlag);
        } else if(docFlag == 3){
                    if (st.equals("</HEADLINE>")) {
                        docFlag = 0;
                        return;
                    }
                    if(st.charAt(0)=='<')
                        return;
                    if(st.charAt(0)=='F' && st.charAt(1)=='T'){
                        String[] tokens = split(st, "/");
                        st = tokens[1];
                    }
                    doc.update(st, docFlag);
        } else {
            if (contains(st, "<DOCNO>")) {
                st = st.replaceAll(" ", "");
                st = st.substring(7, st.length() - 8);
                doc.update(st, 1);
                return;
            } else if (contains(st, "<F P=104>")) {
                findCityID(st);
                return;
            } else if (st.equals("<TEXT>")) {
                docFlag = 4;
                return;
            } else if (st.equals("</DOC>")) {
                doc.setDocFile(fileID);
                docSet.add(doc);
                doc = new Doc();
                docFlag = 0;
                return;
            } else if (contains(st, "<TI>")){
                findDocTitle(st);
            } else if (contains(st, "<HEADLINE>")){
                docFlag = 3;
            }
        }
    }

    /**
     * Find within the line the specific cityID.
     * @param st - The line contain <F P=104>, and the city ID.
     */
    private void findCityID(String st) {
        String [] array = split(st," ");
        StringBuilder tmp = new StringBuilder();
        if(array.length > 3) {
            tmp.append(array[2]).append(" ").append(array[3]);
            doc.update(tmp.toString(), 2);
        }
    }

    private void findDocTitle(String s){
        String [] tokens = split(s," ");
        StringBuilder tmp = new StringBuilder();
        int idx = 0;
        while (!tokens[idx].equals("<TI>"))
            idx++;
        idx++;
        while (!tokens[idx].equals("</TI></H3>")){
            tmp.append(tokens[idx]).append(" ");
            idx++;
        }
        doc.update(tmp.toString(),3);
    }

    public void resetDocSet()
    {
        docSet.clear();
    }

    public int getListOfFilesSize() {
        if(listOfFiles == null)
            return 0;
        else
            return listOfFiles.length;
    }

}
