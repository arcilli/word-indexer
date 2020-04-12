package parallelization;

import com.mongodb.client.MongoClients;
import lombok.extern.java.Log;
import model.indexes.DirectIndexDoc;
import model.indexes.ReverseIndexDoc;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import text.splitter.TextSplitter;

import java.util.HashMap;
import java.util.List;

@Log
public class Worker implements Runnable {
    private String filePath;
    private IndexType indexType;

    private MongoOperations mongoOps = new MongoTemplate(MongoClients.create(), "test");

    public Worker(String filePath, IndexType indexType) {
        this.filePath = filePath;
        this.indexType = indexType;
    }

    @Override
    public void run() {
        if (IndexType.DIRECT == indexType) {
            try {
                HashMap<String, Integer> wordNoOcc = TextSplitter.createDirectIndexFromFile(filePath);
                DirectIndexDoc directIndexDoc = new DirectIndexDoc(filePath, null);
                directIndexDoc.convert(wordNoOcc);
                directIndexDoc.persistDirectIndexModel(mongoOps);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (IndexType.REVERSE == indexType) {
            log.info("Getting direct index & revert for: " + filePath);
            DirectIndexDoc directIndexDoc = getDirectIndexDocForFile(filePath);
            if (null != directIndexDoc) {
                log.info("Persist file: " + filePath + " using individual entities");
                ReverseIndexDoc.persistPartialReverseIndex(mongoOps, directIndexDoc);
            }
        }
    }

    private DirectIndexDoc getDirectIndexDocForFile(String file) {
        List<DirectIndexDoc> docs = mongoOps.find(Query.query(Criteria.where("file").is(file)), DirectIndexDoc.class);
        if (1 == docs.size()) {
            return docs.get(0);
        }
        return null;
    }
}