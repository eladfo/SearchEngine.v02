package Model;
import java.io.*;
import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.split;


public class ReadFile {
    private File folder;
    private File[] listOfFiles;
    public Doc doc;
    private HashSet<Doc> docSet;
    private int docFlag;
    private int chunk;

    public ReadFile(String corpusPath) throws IOException {
        folder = new File(corpusPath);
        listOfFiles = folder.listFiles();
        docSet = new HashSet<>();
        doc=null ;
        docFlag = 0;
//        chunk = listOfFiles.length/partitions;
        chunk = 50;
    }

    public HashSet<Doc> readLines(int extIdx) throws IOException {
            for (int i = extIdx*chunk; i<listOfFiles.length && i<chunk*(extIdx+1); i++){
                doc = new Doc();
                File file = new File(listOfFiles[i] + "\\" + listOfFiles[i].getName());
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
        if (st.length()>7 && st.length()<50 && st.substring(0, 7).equals("<DOCNO>")) {
            st = st.replaceAll(" ", "");
            st = st.substring(7, st.length() - 8);
            doc.update(st, 1);
            return;
        } else if (st.length()>9 && st.substring(0, 9).equals("<F P=104>")) {
            find_city(st);
            return;
        }
        else if (st.length()>9 && st.substring(0, 9).equals("<F P=105>")) {
            //System.out.println("langgg" + st);
            return;
        }else if (st.equals("<HEADER>")) {
            docFlag = 0;
            return;
        } else if (st.equals("</HEADER>") || st.equals("</TEXT>")) {
            docFlag = 0;
            return;
        } else if (st.equals("<TEXT>")) {
            docFlag = 4;
            return;
        } else if (st.equals("</DOC>")){
            docSet.add(doc);
            doc = new Doc();
            return;
        }
        doc.update(st, docFlag);
    }

    private void find_city(String st) {
        String [] array = split(st," ");
        StringBuilder tmp = new StringBuilder();

        if(array.length > 3) {
            tmp.append(array[2]).append(" ").append(array[3]);
            doc.update(tmp.toString(), 2);
        }
    }

    public File[] getListOfFiles() {
        return listOfFiles;
    }
}
