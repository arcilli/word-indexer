package model.indexes;

import model.DocNoOccTuple;
import model.WordNoOccTuple;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Document
public class ReverseIndexDoc {
    private String word;
    private static DocNoOccTuple docNoOccTuple = new DocNoOccTuple("", -1);
    private List<DocNoOccTuple> listOfDocuments;

    public ReverseIndexDoc(String word, List<DocNoOccTuple> listOfDocuments) {
        this.word = word;
        this.listOfDocuments = listOfDocuments;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<DocNoOccTuple> getListOfDocuments() {
        return listOfDocuments;
    }

    public void setListOfDocuments(List<DocNoOccTuple> listOfDocuments) {
        this.listOfDocuments = listOfDocuments;
    }

    /***
     * Insert the word in reverseIndex if it is not presend. Otherwise, append the document to the list of documents.
     * @param directIndexDoc
     */
    public static void persistPartialReverseIndex(MongoOperations mongoOps, DirectIndexDoc directIndexDoc) {
        String file = directIndexDoc.getFile();

        for (WordNoOccTuple wordNoOccTuple : directIndexDoc.getListOfWords()) {
            String word = wordNoOccTuple.getWord();
            Integer noOcc = wordNoOccTuple.getNoOcc();
            Query query = new Query();
            query.addCriteria(Criteria.where("word").is(word));
            Update update = new Update();

            // Use prototype.
            docNoOccTuple.setDocumentName(file);
            docNoOccTuple.setNoOcc(noOcc);

            update.addToSet("listOfDocuments", docNoOccTuple);
            mongoOps.upsert(query, update, ReverseIndexDoc.class);
        }
    }
}