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
    private int chunk;
    private String fileID;
    private String corpusPath;

    public ReadFile(String corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        folder = new File(corpusPath);
        listOfFiles = folder.listFiles();
        docSet = new HashSet<>();
        doc=null ;
        docFlag = 0;
        chunk = 50;
    }

    public HashSet<Doc> readLines(int extIdx) throws IOException {
            for (int i = extIdx*chunk; i<listOfFiles.length && i<chunk*(extIdx+1); i++){
                doc = new Doc();
                File innerFolder = new File(listOfFiles[i].getPath());
                File[] innerFiles = innerFolder.listFiles();
                File file = new File(listOfFiles[i] + "\\" + innerFiles[0].getName());
                fileID = innerFiles[0].getName();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null)
                    if (!st.isEmpty()) {
                        stringBuild(st);
                    }
            }
        return docSet;
    }

    public void stringBuild(String st) {
        if (contains(st,"<DOCNO>")) {
            st = st.replaceAll(" ", "");
            st = st.substring(7, st.length() - 8);
            doc.update(st, 1);
            return;
        } else if (contains(st,"<F P=104>")) {
            findCityID(st);
            return;
        }else if (st.equals("<HEADER>") || st.equals("</TEXT>")) {
            docFlag = 0;
            return;
        } else if (st.equals("<TEXT>")) {
            docFlag = 4;
            return;
        } else if (st.equals("</DOC>")){
            doc.docFile = fileID;
            docSet.add(doc);
            doc = new Doc();
            docFlag = 0;
            return;
        } else if (st.equals("<P>") || st.equals("</P>") || contains(st,"<F P=105>")|| contains(st,"Article Type"))
            return;

        doc.update(st, docFlag);
    }

    private void findCityID(String st) {
        String [] array = split(st," ");
        StringBuilder tmp = new StringBuilder();
        if(array.length > 3) {
            tmp.append(array[2]).append(" ").append(array[3]);
            doc.update(tmp.toString(), 2);
        }
    }

    public void resetDocSet()
    {
        docSet.clear();
    }

    public int getListOfFilesSize() {
        if(listOfFiles == null)
            return 0;
        else
            return listOfFiles.length;};

}
