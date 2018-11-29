package Model;

import java.util.Map;
import java.util.TreeMap;

public class Term {

    public TreeMap<String,StringBuilder> docList;
    public  int flag;
    /** df = TreemMap.size() **/

    public Term(String str, StringBuilder count, int f) {

        docList = new TreeMap<>();
        flag = f;
        addDoc(str, count);
    }

    public void addDoc(String s, StringBuilder count){
        docList.put(s, count);
    }

    public StringBuilder from_term_to_string()
    {
        StringBuilder s = new StringBuilder();
        for(Map.Entry<String,StringBuilder> entry : docList.entrySet())
        {

            s.append(entry.getKey()).append(",").append(entry.getValue().append("~"));
        }
        s.append("\n");
        return s;
    }
}
