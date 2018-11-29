package Model;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Stemmer
{
    /**
     * using snowball stemmer we stem the words
     */

    SnowballStemmer ps;
    public Stemmer() {
        ps = new englishStemmer();
    }

    public String stem(String str){
        ps.setCurrent(str);
        ps.stem();
        String result = ps.getCurrent();
        return result;
    }
}