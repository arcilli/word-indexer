package stemmer;

public class Stemmer extends LovinsStemmer {

    public String stem(String str) {
        str = str.toLowerCase();
        if (str.length() <= 2) {
            return str;
        }
        String stemmed = super.stem(str);
        while (!stemmed.equals(str)) {
            str = stemmed;
            stemmed = super.stem(stemmed);
        }
        return stemmed;
    }
}
