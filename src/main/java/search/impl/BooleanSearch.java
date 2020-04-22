package search.impl;

import lombok.extern.java.Log;
import org.springframework.lang.Nullable;
import search.QueryProcessor;
import search.Search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Log
public class BooleanSearch implements Search {

    HashMap<String, HashMap<String, Integer>> reverseIndexFromFiles = null;

    public BooleanSearch(@Nullable String... filePath) {
        if (null != filePath) {
            if (1 == filePath.length) {
                reverseIndexFromFiles = this.loadReverseIndexFromFile(filePath[0]);
            } else {
                System.out.println("Boolean search is using DB.");
            }
        }
    }

    @Override
    public Set<String> search(String query) {
        QueryProcessor queryProcessor = new QueryProcessor();
        try {
            queryProcessor.parse(query);
            // Get the list of documents that contains the first term.
            // This is considered as initial result.
            Set<String> result = getFilesContainingTerm(queryProcessor.queryTerms.remove());
            while (!queryProcessor.queryTerms.isEmpty()
                    && !queryProcessor.operations.isEmpty()) {
                String term2 = queryProcessor.queryTerms.remove();
                Character operator = queryProcessor.operations.remove();
                result = computeOperation(operator, result, term2);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<String> orOperation(Set<String> filesContainingFirstKey, String key2) {
        Set<String> filesContainingSecondKey = getFilesContainingTerm(key2);
        if (filesContainingFirstKey.size() > filesContainingSecondKey.size()) {
            for (String file : filesContainingSecondKey) {
                if (!filesContainingFirstKey.contains(file)) {
                    filesContainingFirstKey.add(file);
                }
            }
            return filesContainingFirstKey;
        } else {
            for (String file : filesContainingFirstKey) {
                if (!filesContainingSecondKey.contains(file)) {
                    filesContainingSecondKey.add(file);
                }
            }
            return filesContainingSecondKey;
        }
    }

    /**
     * @return a list of documents that contains both keys
     */
    private Set<String> andOperation(Set<String> filesContainingFirstKey, String key2) {
        Set<String> filesContainingSecondKey = getFilesContainingTerm(key2);

        if (filesContainingFirstKey.size() < filesContainingSecondKey.size()) {
            filesContainingFirstKey.removeIf(file -> !filesContainingSecondKey.contains(file));
            return filesContainingFirstKey;
        } else {
            filesContainingSecondKey.removeIf(file -> !filesContainingFirstKey.contains(file));
            return filesContainingSecondKey;
        }
    }

    /**
     * @return documents that contains key1 and not contain key2
     */
    private Set<String> notOperation(Set<String> filesContainingFirstKey, String key2) {
        Set<String> filesContainingSecondKey = getFilesContainingTerm(key2);
        for (String file : filesContainingSecondKey) {
            if (filesContainingFirstKey.contains(file)) {
                filesContainingFirstKey.remove(file);
            }
        }
        return filesContainingFirstKey;
    }

    private Set<String> computeOperation(Character operator, Set<String> files, String term2) {
        switch (operator) {
            case '|':
                return orOperation(files, term2);
            case '&':
                return andOperation(files, term2);
            case '~':
                return notOperation(files, term2);
            default:
                System.out.println("Unsupported character in the query.");
                return null;
        }
    }

    private Set<String> getFilesContainingTerm(String term) {
        if (null != reverseIndexFromFiles) {
            return new HashSet<>(reverseIndexFromFiles.get(term).keySet());
        }
        // Use database for retrieving the files.
        return this.getFilesContainingTermFromDB(term);
    }
}
