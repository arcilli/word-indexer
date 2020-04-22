package model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.DirectIndexTuple;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Document
@AllArgsConstructor
@Getter
@Setter

public class DirectIndexDoc {
    private String file;
    private List<DirectIndexTuple> listOfWords;

    public List<DirectIndexTuple> convert(HashMap<String, Integer> hashMap) {
        int noWords = this.getNumberOfWords(hashMap);
        listOfWords = new LinkedList<>();
        for (String key : hashMap.keySet()) {
            Double termFrequency = (double) hashMap.get(key) / (double) noWords;
            listOfWords.add(new DirectIndexTuple(key, hashMap.get(key), termFrequency));
        }
        return listOfWords;
    }

    // adapter
    public HashMap<String, Integer> convert(List<DirectIndexTuple> directIndexTuples) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (DirectIndexTuple directIndexTuple : directIndexTuples) {
            hashMap.put(directIndexTuple.getWord(), directIndexTuple.getNoOcc());
        }
        return hashMap;
    }

    public void persistDirectIndexModel(MongoOperations mongoOps) {
        mongoOps.insert(this);
    }

    private int getNumberOfWords(HashMap<String, Integer> hashMap) {
        int noWords = 0;
        for (String key : hashMap.keySet()) {
            noWords += hashMap.get(key);
        }
        return noWords;
    }
}
