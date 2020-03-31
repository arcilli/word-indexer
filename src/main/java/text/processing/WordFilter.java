package text.processing;

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

    public WordFilter() throws FileNotFoundException {
        loadFromFile();
    }


    public boolean shouldBeStored(String word) {
        if (exceptions.contains(word.toLowerCase())) {
            return true;
        } else if (!stopWords.contains(word.toLowerCase())) {
            //string getBaseFrom()
            return true;
        }
        return false;
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
