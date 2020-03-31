package search;

import com.google.gson.stream.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

public interface Search {

    // TODO: move to index package.
    default HashMap<String, HashMap<String, Integer>> loadReverseIndexFromFile(String filePath) {
        HashMap<String, HashMap<String, Integer>> reverseIndex = new HashMap<>();
        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(filePath)));

            // Initial object.
            // Each word from reverse index is treated as an attribute in the initial object.
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                // Each word from reverse index.
                String word = jsonReader.nextName();
                HashMap<String, Integer> filesContainingCurrentWord = new HashMap<>();
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    filesContainingCurrentWord.put(jsonReader.nextName(), jsonReader.nextInt());
                }
                jsonReader.endObject();
                reverseIndex.put(word, filesContainingCurrentWord);
            }
            jsonReader.endObject();
            return reverseIndex;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reverseIndex;
    }

    Set<String> search(String query);

}
