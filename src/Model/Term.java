package Model;
import java.util.Map;
import java.util.TreeMap;

public class Term {

    public TreeMap<String,StringBuilder> docList;
    public int flag;
    /**
     * Constructor. Add the first doc that had the Term, and also his positions.
     * @param str - docID
     * @param count - term's position in doc
     * @param f - 1 if word is capital letters, else 0.
     */
    public Term(String str, StringBuilder count, int f) {
        docList = new TreeMap<>();
        flag = f;
        if(str != null)
            addDoc(str, count);
    }

    /**
     * Add a doc and the positions of the term, to the doc list.
     * @param s - docID
     * @param count - term's position in doc
     */
    public void addDoc(String s, StringBuilder count){

        docList.put(s, new StringBuilder(count.toString()));
    }

    /**
     *
     * @return A list of all the documents that contained this term, and also his locations in the docs.
     * Pattern: docID,positions~docID1,positions~    etc..
     */
    public StringBuilder getTermDocList() {
        StringBuilder s = new StringBuilder();
        for(Map.Entry<String,StringBuilder> entry : docList.entrySet())
        {
            s.append(entry.getKey()).append(",").append(entry.getValue().toString()).append("~");
        }
        s.append("\n");
        return s;
    }
}
