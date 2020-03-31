import index.Indexer;
import org.bson.Document;
import search.impl.BooleanSearch;
import text.splitter.TextSplitter;
import webpage.parser.PageInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public class MainRIW {

    public static void Lab01() throws IOException {
        PageInfo pageInfo = new PageInfo("https://www.tuiasi.ro/");
        System.out.println(pageInfo.getTitle());
        System.out.println(pageInfo.getKeywords());
        System.out.println(pageInfo.getDescription());
        System.out.println(pageInfo.getRobots());
        System.out.println(pageInfo.getLinks());
        System.out.println(pageInfo.getText());
        TextSplitter.createDirectIndexFromFile("files/inputs/7.txt");

    }

    public static void Lab02() throws IOException {
        Indexer directoryProcessing = new Indexer(new File("files/inputs"));
        directoryProcessing.createDirectIndexAndMapFiles();
    }

    public static void Lab03() throws IOException {
        Lab02();
        Indexer directoryProcessing = new Indexer(new File("output/files/inputs"));
        directoryProcessing.createReverseIndex();
    }

    public static void Lab04() throws IOException {
        Indexer directoryProcessing = new Indexer(
                new File("files/inputs2"));
        directoryProcessing.createDirectIndexAndMapFiles();

        directoryProcessing = new Indexer(
                new File("output/files/inputs2"));
        directoryProcessing.createReverseIndex();

//         Suppose that the reverse index is already created.
        while (true) {
            System.out.println("Write a query: ");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));
            String query = reader.readLine();
            if (query.equals("exit")) {
                System.out.println("Bye:)");
                break;
            }
            BooleanSearch booleanSearch = new BooleanSearch();
            System.out.println("Result: " + booleanSearch.search(query));
        }
    }

    public static void Lab05() {
        try {
            // Direct index
            Indexer indexer = new Indexer(new File("files/inputs"));
            indexer.createDirectIndexAndMapFiles();
            indexer.persistDirectIndex();

            // Create reverse index
            indexer = new Indexer(
                    new File("output/files/inputs"));
            HashMap<String, HashMap<String, Integer>> reverseIndex = indexer.createReverseIndex();
            List<Document> reverseIndexJson = indexer.createJsonForReverseIndex(reverseIndex);
            // Persist it
            indexer.persistReverseIndexJson(reverseIndexJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
//        Lab01();
//        Lab02();
//        Lab03();
//        Lab04();
        Lab05();
    }
}
