package search;

import com.google.gson.stream.JsonReader;
import com.mongodb.client.MongoClients;
import com.mongodb.lang.Nullable;
import model.ReverseIndexTuple;
import model.indexes.ReverseIndexDoc;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Search {

    @Nullable
    Set<String> search(String query);

    /**
     * Retrieve files that contains a term from DB.
     * It's using reverseIndexDoc collection, it should be already populated.
     */
    @Nullable
    default Set<String> getFilesContainingTermFromDB(String term) {
        Query query = new Query();
        query.addCriteria(Criteria.where("word").is(term));
        MongoOperations mongoOps = new MongoTemplate(MongoClients.create(), "test");
        List<ReverseIndexDoc> docs = mongoOps.find(query, ReverseIndexDoc.class, "reverseIndexDoc");
        Set<String> files = new HashSet<>();

        // Cover the case when same word has many records in the database.
        for (ReverseIndexDoc entry : docs) {
            for (ReverseIndexTuple reverseIndexTuple : entry.getListOfDocuments()) {
                files.add(reverseIndexTuple.getDocumentName());
            }
        }
        return files;
    }

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
}
