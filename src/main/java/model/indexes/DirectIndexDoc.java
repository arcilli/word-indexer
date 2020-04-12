package model.indexes;

import model.WordNoOccTuple;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Document
public class DirectIndexDoc {
    private String file;
    private List<WordNoOccTuple> listOfWords;

    public DirectIndexDoc(String file, List<WordNoOccTuple> listOfWords) {
        this.file = file;
        this.listOfWords = listOfWords;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<WordNoOccTuple> getListOfWords() {
        return listOfWords;
    }

    public void setListOfWords(List<WordNoOccTuple> listOfWords) {
        this.listOfWords = listOfWords;
    }

    // adapter
    public List<WordNoOccTuple> convert(HashMap<String, Integer> hashMap) {
        listOfWords = new LinkedList<>();
        for (String key : hashMap.keySet()) {
            listOfWords.add(new WordNoOccTuple(key, hashMap.get(key)));
        }
        return listOfWords;
    }

    // adapter
    public HashMap<String, Integer> convert(List<WordNoOccTuple> wordNoOccTuples) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (WordNoOccTuple wordNoOccTuple : wordNoOccTuples) {
            hashMap.put(wordNoOccTuple.getWord(), wordNoOccTuple.getNoOcc());
        }
        return hashMap;
    }

    public void persistDirectIndexModel(MongoOperations mongoOps) {
        mongoOps.insert(this);
    }
}
