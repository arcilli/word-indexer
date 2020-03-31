package text.splitter;

import stemmer.Stemmer;
import text.processing.WordFilter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

public class TextSplitter {
    private Stemmer stemmer = new Stemmer();

    public static HashMap<String, Integer> createDirectIndexFromFile(String fileName) throws IOException {
        HashMap<String, Integer> wordCount = new HashMap<>();
        WordFilter wordFilter = new WordFilter();
        try {
            InputStream file = new FileInputStream(fileName);
            Reader reader = new InputStreamReader(file, Charset.defaultCharset());

            int character;
            StringBuilder stringBuilder = new StringBuilder();
            while ((character = reader.read()) != -1) {
                char ch = (char) character;
                if (Character.isLetterOrDigit(ch)) {
                    stringBuilder.append(ch);
                } else {
                    // Just found a word.
                    String wordToBeStored = wordFilter.storeIfExceptionOrDictionary(stringBuilder.toString().toLowerCase());
                    if (null != wordToBeStored) {
                        if (wordCount.containsKey(wordToBeStored)) {
                            wordCount.put(wordToBeStored,
                                    wordCount.get(wordToBeStored) + 1);
                        } else {
                            wordCount.put(wordToBeStored, 1);
                        }
                    }
                    stringBuilder.setLength(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        wordCount.remove("");
        return wordCount;
    }
}