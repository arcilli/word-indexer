package search;

import text.processing.WordFilter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class QueryProcessor {

    // Use *public* fields since these will be modified while processing term by term.
    public Queue<String> queryTerms;
    public Queue<Character> operations;

    public void parse(String originalQuery) {

        // Izolare cuvinte și izolare operatori.
        // Filtrare cuvinte & transformare in lowercase.
        queryTerms = new LinkedList<>();
        operations = new LinkedList<>();

        StringBuilder stringBuilder = new StringBuilder();
        try {
            WordFilter wordFilter = new WordFilter();
            for (int i = 0; i < originalQuery.length(); ++i) {
                if (Character.isLetterOrDigit(originalQuery.charAt(i))) {
                    stringBuilder.append(originalQuery.charAt(i));
                } else {
                    if (Character.isLetterOrDigit(originalQuery.charAt(i))) {
                        stringBuilder.append(originalQuery.charAt(i));
                    } else {
                        // Just found a sign.
                        String word = wordFilter.storeIfExceptionOrDictionary(stringBuilder.toString().toLowerCase());
                        if (null != word) {
                            queryTerms.add(word);
                        }
                        // Add the sign to operation list.
                        operations.add(originalQuery.charAt(i));
                        stringBuilder.setLength(0);
                    }
                }
            }
            String word = wordFilter.storeIfExceptionOrDictionary(stringBuilder.toString().toLowerCase());
            if (null != word) {
                queryTerms.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error at loading stopwords list");
        }
    }
}
