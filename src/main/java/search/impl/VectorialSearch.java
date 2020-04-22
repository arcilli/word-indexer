package search.impl;

import com.mongodb.client.MongoClients;
import lombok.extern.java.Log;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import search.QueryProcessor;
import search.Search;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Log
public class VectorialSearch implements Search {
    private QueryProcessor queryProcessor = new QueryProcessor();
    private MongoOperations mongoOps = new MongoTemplate(MongoClients.create(), "test");
    private static BooleanSearch booleanSearch = new BooleanSearch();

    @Override
    @Nullable
    /**
     * Returns an ordered Set with interested words.
     */
    public Set<String> search(String query) {
        Set<String> documentsAfterBooleanSearch = booleanSearch.search(query);
        System.out.println("Transforming documents into documentVectors considering query terms.");
        queryProcessor.parse(query);
        assert documentsAfterBooleanSearch != null;
        HashMap<String, HashMap<String, Double>> documentVectors = computeDocumentTfIdf(
                new ArrayList<>(queryProcessor.queryTerms), documentsAfterBooleanSearch);

        HashMap<String, Double> queryVector = new HashMap<>();
        for (String term : queryProcessor.queryTerms) {
            queryVector.put(term, getIdf(term) * getQueryTf(term, queryProcessor.queryTerms));
        }

        HashMap<String, Double> searchResultsSimilarity = new HashMap<>();
        for (String doc : documentVectors.keySet()) {
            double cosineSimilarityVal = cosineSimilarity(documentVectors.get(doc), queryVector);
            if (0 != cosineSimilarityVal) {
                searchResultsSimilarity.put(doc, cosineSimilarityVal);
            }
        }
        searchResultsSimilarity = sortHashMapByValue(searchResultsSimilarity);
        System.out.println(searchResultsSimilarity);
        return searchResultsSimilarity.keySet();
    }

    private static double getQueryTf(String word, Queue<String> queryTerms) {
        int noApparitions = 0;
        for (String term : queryTerms) {
            if (word.equals(term)) {
                ++noApparitions;
            }
        }
        return (double) noApparitions / (double) queryTerms.size();
    }

    private static double cosineSimilarity(HashMap<String, Double> documentTfIdf, HashMap<String, Double> queryDocumentTfIdf) {
        double dotProduct = 0;
        double sumSquaresDoc1 = 0;
        double sumSquaresDoc2 = 0;
        double tfIdf1;
        double tfIdf2;

        boolean haveOneOrMoreWordInCommon = false;

        for (String word : queryDocumentTfIdf.keySet()) {
            if (documentTfIdf.containsKey(word)) {
                haveOneOrMoreWordInCommon = true;
                tfIdf1 = documentTfIdf.get(word);
                tfIdf2 = queryDocumentTfIdf.get(word);
                dotProduct += Math.abs(tfIdf1 * tfIdf2);
                sumSquaresDoc1 += tfIdf1 * tfIdf1;
                sumSquaresDoc2 += tfIdf2 * tfIdf2;
            }
        }

        if (!haveOneOrMoreWordInCommon || 0 == dotProduct || 0 == sumSquaresDoc1 || 0 == sumSquaresDoc2) {
            return 0;
        }
        double numitor = Math.sqrt(sumSquaresDoc1) * Math.sqrt(sumSquaresDoc2);
        double numarator = Math.abs(dotProduct);
        return numarator / numitor;
    }

    private double getTf(String key, String doc) {
        //faci cautare in indexul direct aferent documentului doc
        Query query = new Query();
        query.addCriteria(Criteria.where("word").is(key));
        Document unmappedDocuments = mongoOps.findOne(query, Document.class, "reverseIndexDoc");
        List<Document> listOfDocumentsUnmapped = (List<Document>) unmappedDocuments.get("listOfDocuments");
        for (Document unmappedDoc : listOfDocumentsUnmapped) {
            if (unmappedDoc.get("documentName").equals(doc)) {
                return unmappedDoc.getDouble("termFrequency");
            }
        }
        return 0.0;
    }

    private HashMap<String, HashMap<String, Double>> computeDocumentTfIdf(List<String> queryTerms, Set<String> docs) {
        HashMap<String, HashMap<String, Double>> map = new HashMap<>();
        for (String doc : docs) {
            HashMap<String, Double> docTfMulIdf = new HashMap<>();
            for (String key : queryTerms) {
                docTfMulIdf.put(key, getTf(key, doc) * getIdf(key));
            }
            map.put(doc, docTfMulIdf);
        }
        return map;
    }

    private long getNumberOfIndexedDocs() {
        Query query = new Query();
        query.fields().include("file");
        query.fields().exclude("_id");
        return mongoOps.count(query, "directIndexDoc");
    }

    private Double getIdf(String word) {
        double numarator = getNumberOfIndexedDocs();
        double numitor = 1.0 + countNoDocumentsContainingWordFromInputCollection(word);
        return log(numarator / numitor);
    }

    private int countNoDocumentsContainingWordFromInputCollection(String word) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("word").is(word)),
                project()
                        .and("listOfDocuments")
                        .size()
                        .as("count")
        );
        AggregationResults<Document> results = mongoOps.aggregate(aggregation, "reverseIndexDoc", Document.class);
        return results.getMappedResults().get(0).getInteger("count");
    }

    // function to sort hashmap by values
    private static HashMap<String, Double> sortHashMapByValue(HashMap<String, Double> hashMap) {
        return hashMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }
}