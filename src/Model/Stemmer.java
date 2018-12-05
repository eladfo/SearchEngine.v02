package Model;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Stemmer
{
    /**
     * Using SnowBall stemmer (v.1.3.0.581).
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