package model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.DirectIndexTuple;
import model.ReverseIndexTuple;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class ReverseIndexDoc {
    private String word;
    private List<ReverseIndexTuple> listOfDocuments;

    // Use the same pre-created object instead of creating a new one every time.
    private static ReverseIndexTuple reverseIndexTuple = new ReverseIndexTuple("", -1, -1.0);
    private Double inverseDocumentFrequency;

    /***
     * Insert the word in reverseIndex if it is not present. Otherwise, append the document to the list of documents.
     * @param directIndexDoc
     */
    public static void persistPartialReverseIndex(MongoOperations mongoOps, DirectIndexDoc directIndexDoc) {
        String file = directIndexDoc.getFile();
        for (DirectIndexTuple directIndexTuple : directIndexDoc.getListOfWords()) {
            String word = directIndexTuple.getWord();
            Integer noOcc = directIndexTuple.getNoOcc();
            Double tf = directIndexTuple.getTermFrequency();

            Query query = new Query();
            query.addCriteria(Criteria.where("word").is(word));
            Update update = new Update();

            // Use prototype.
            reverseIndexTuple.setDocumentName(file);
            reverseIndexTuple.setNoOcc(noOcc);
            reverseIndexTuple.setTermFrequency(tf);

            update.addToSet("listOfDocuments", reverseIndexTuple);
            mongoOps.upsert(query, update, ReverseIndexDoc.class);
        }
    }
}