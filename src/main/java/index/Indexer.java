package index;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import text.splitter.TextSplitter;

import java.io.*;
import java.util.*;

public class Indexer {
    private Queue<File> directoryQueue;

    public Indexer(File directory) {
        directoryQueue = new LinkedList<>();
        directoryQueue.add(directory);
    }

    public void createDirectIndexAndMapFiles() throws IOException {
        int fileNumber = 0;

        File mapFile = new File("output/mapfiles/directIndexMap.txt");
        createNewFileWithLocation(mapFile);
        PrintWriter mapWriter = new PrintWriter(mapFile);

        Queue<File> directoryQueueCopy = new LinkedList<>(directoryQueue);
        while (!directoryQueueCopy.isEmpty()) {
            File directory = directoryQueueCopy.remove();
            File[] filesFromDirectory = directory.listFiles();
            String output = "output/" + directory + "/" + "b" + fileNumber + ".idx";

            File outputFile = new File(output);
            createNewFileWithLocation(outputFile);

            PrintWriter printWriter = new PrintWriter(outputFile);
            if (null != filesFromDirectory) {
                for (File file : filesFromDirectory) {
                    if (file.isFile()) {
                        printWriter.println(file.toString());
                        System.out.println("Processing: " + file.toString());
                        HashMap<String, Integer> hashMap = TextSplitter.createDirectIndexFromFile(file.toString());

                        for (String key : hashMap.keySet()) {
                            printWriter.println(key + " " + hashMap.get(key));
                        }

                        // Write map file.
                        mapWriter.println(file.toString() + " " + output);
                    } else if (file.isDirectory()) {
                        directoryQueueCopy.add(file);
                    }
                }
            }
            printWriter.close();
            ++fileNumber;
        }
        mapWriter.close();
    }

    public HashMap<String, HashMap<String, Integer>> parseDirectIndex(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        HashMap<String, Integer> wordHashMap = null;
        HashMap<String, HashMap<String, Integer>> map = new HashMap<>();

        String line = bufferedReader.readLine();
        while (line != null) {
            if (line.contains(" ")) {
                int spaceIndex = line.indexOf(" ");
                String word = line.substring(0, spaceIndex);

                Integer count = Integer.parseInt(line.substring(spaceIndex + 1));
                assert null != wordHashMap;
                wordHashMap.put(word, count);
            } else if (line.length() > 0) {
                wordHashMap = new HashMap<>();
                map.put(line, wordHashMap);
            }
            line = bufferedReader.readLine();
        }
        return map;
    }

    // Assumes that the direct index is already created.
    public HashMap<String, HashMap<String, Integer>> createReverseIndex() throws IOException {
        HashMap<String, HashMap<String, Integer>> reverseIndex = new HashMap<>();
        File mapFile = new File("output/mapfiles/reverseIndexMap.txt");
        createNewFileWithLocation(mapFile);
        PrintWriter mapWriter = new PrintWriter(mapFile);

        HashMap<String, List<String>> mapList = new HashMap<>();

        Queue<File> directoryQueueCopy = new LinkedList<>(directoryQueue);
        while (!directoryQueueCopy.isEmpty()) {
            File currentDirectory = directoryQueueCopy.remove();
            File[] filesFromCurrentDirectory = currentDirectory.listFiles();
            if (null != filesFromCurrentDirectory) {
                for (File file : filesFromCurrentDirectory) {
                    if (file.isDirectory()) {
                        directoryQueueCopy.add(file);
                    } else {
                        // Process a file.
                        HashMap<String, HashMap<String, Integer>> mapHashMap = parseDirectIndex(file.toString());

                        for (Map.Entry<String, HashMap<String, Integer>> mapElement : mapHashMap.entrySet()) {
                            HashMap<String, Integer> elements = mapElement.getValue();
                            String docName = mapElement.getKey();
                            for (HashMap.Entry<String, Integer> entry : elements.entrySet()) {

                                String word = entry.getKey();
                                int noOccurrences = entry.getValue();

                                if (reverseIndex.containsKey(word)) {
                                    HashMap<String, Integer> docCounter = reverseIndex.get(word);
                                    docCounter.put(docName, noOccurrences);
                                    reverseIndex.put(word, docCounter);
                                } else {
                                    HashMap<String, Integer> docCounter = new HashMap<>();
                                    docCounter.put(docName, noOccurrences);
                                    reverseIndex.put(word, docCounter);
                                }
                                if (!mapList.containsKey(word)) {
                                    mapList.put(word, new LinkedList<>());
                                }
                                if (!mapList.get(word).contains(file.toString())) {
                                    mapList.get(word).add(file.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        Gson builder = new Gson().newBuilder().setPrettyPrinting().create();
        mapWriter.println(builder.toJson(mapList));
        for (HashMap.Entry<String, List<String>> element : mapList.entrySet()) {
            System.out.println(element.getKey() + " " + element.getValue());
            mapWriter.println(element.getKey() + " " + element.getValue());
        }

        File reverseIndexFile = new File("output/reverseIndex/reverseIndex.txt");
        createNewFileWithLocation(reverseIndexFile);
        PrintWriter reverseIndexWriter = new PrintWriter(reverseIndexFile);

        reverseIndexWriter.println(builder.toJson(reverseIndex));
        mapWriter.close();
        reverseIndexWriter.close();
        return reverseIndex;
    }

    private void createNewFileWithLocation(File outputFile) throws IOException {
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
    }

    public void persistDirectIndex() throws IOException {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection("directIndex");
        Queue<File> directoryQueueCopy = new LinkedList<>(directoryQueue);
        while (!directoryQueueCopy.isEmpty()) {
            File directory = directoryQueueCopy.remove();
            File[] filesFromDirectory = directory.listFiles();
            if (null != filesFromDirectory) {
                for (File file : filesFromDirectory) {
                    if (file.isDirectory()) {
                        directoryQueueCopy.add(file);
                    } else {
                        Document document = createWordCountJsonForFile(TextSplitter.createDirectIndexFromFile(file.toString()),
                                file.toString());
                        collection.insertOne(document);
                    }
                }
            }
        }
    }

    private Document createWordCountJsonForFile(HashMap<String, Integer> wordCount, String fileName) {
        Document document = new Document("doc", fileName);
        List<Document> array = new ArrayList<>();
        for (String string : wordCount.keySet()) {
            array.add(new Document(string, wordCount.get(string)));
        }
        document.put("terms", array);
        return document;
    }

    public List<Document> createJsonForReverseIndex(HashMap<String, HashMap<String, Integer>> reverseIndex) {
        List<Document> documentsToBeInserted = new ArrayList<>();
        for (String word : reverseIndex.keySet()) {
            Document document = new Document();
            document.put("term", word);
            HashMap<String, Integer> docNoOcc = reverseIndex.get(word);
            List<Document> array = new ArrayList<>();
            for (String doc : docNoOcc.keySet()) {
                array.add(new Document(doc.replaceFirst("[.][^.]+$", ""), docNoOcc.get(doc)));
            }
            document.append("docs", array);
            documentsToBeInserted.add(document);
        }
        return documentsToBeInserted;
    }

    public List<Document> persistReverseIndexJson(List<Document> documents) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection("reverseIndex");
        collection.insertMany(documents);
        return documents;
    }
}