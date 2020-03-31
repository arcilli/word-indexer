package text.processing;

import com.mongodb.lang.Nullable;
import stemmer.Stemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class WordFilter {
    private File stopWordsFile = new File("files/stopwords.txt");
    private File exceptionsFile = new File("files/exceptions.txt");
    private List<String> exceptions = new LinkedList<>();
    private List<String> stopWords = new LinkedList<>();
    private Stemmer stemmer = new Stemmer();

    public WordFilter() throws FileNotFoundException {
        loadFromFile();
    }

    @Nullable
    public String storeIfExceptionOrDictionary(String word) {
        if (exceptions.contains(word.toLowerCase())) {
            return word;
        }
        if (!stopWords.contains(word.toLowerCase())) {
            return stemmer.stem(word.toLowerCase());
        }
        return null;
    }

    private void loadFromFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(stopWordsFile);
        while (scanner.hasNext()) {
            stopWords.add(scanner.next());
        }

        scanner = new Scanner(exceptionsFile);
        while (scanner.hasNext()) {
            exceptions.add(scanner.next());
        }
    }
}
