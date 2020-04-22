package parallelization;

import com.mongodb.client.MongoClients;
import model.indexes.DirectIndexDoc;
import model.indexes.ReverseIndexDoc;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import text.splitter.TextSplitter;

import java.util.HashMap;
import java.util.List;

public class Worker implements Runnable {
    private String filePath;
    private WORK_TYPE workType;

    private MongoOperations mongoOps = new MongoTemplate(MongoClients.create(), "test");

    public Worker(String filePath, WORK_TYPE workType) {
        this.filePath = filePath;
        this.workType = workType;
    }

    @Override
    public void run() {
        if (WORK_TYPE.DIRECT_INDEX == workType) {
            try {
                HashMap<String, Integer> wordNoOcc = TextSplitter.createDirectIndexFromFile(filePath);
                DirectIndexDoc directIndexDoc = new DirectIndexDoc(filePath, null);
                directIndexDoc.convert(wordNoOcc);
                directIndexDoc.persistDirectIndexModel(mongoOps);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (WORK_TYPE.REVERSE_INDEX == workType) {
            System.out.println("Getting direct index & revert for: " + filePath);
            DirectIndexDoc directIndexDoc = getDirectIndexDocForFile(filePath);
            if (null != directIndexDoc) {
                System.out.println("Persist file: " + filePath + " using individual entities");
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