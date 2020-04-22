import index.Indexer;
import parallelization.Master;
import search.impl.BooleanSearch;
import search.impl.VectorialSearch;
import text.splitter.TextSplitter;
import webpage.parser.PageInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
        // Direct index is already created.
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

        // Assume that reverse index is already created.
        booleanSearchLoop();
    }

    private static void booleanSearchLoop() throws IOException {
        BooleanSearch booleanSearch = new BooleanSearch();
        while (true) {
            String query = getQuery();
            if (query == null) break;
            System.out.println("Result: " + booleanSearch.search(query));
        }
    }

    private static String getQuery() throws IOException {
        System.out.println("Write a query: ");
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        String query = reader.readLine();
        if (query.equals("exit")) {
            System.out.println("Bye:)");
            return null;
        }
        return query;
    }

    public static void ParallelIndexesCreator() {
        Master master = new Master("files/inputs2");
        master.run();
    }

    public static void Project() {
        VectorialSearch vectorialSearch = new VectorialSearch();
        try {
            while (true) {
                String query = getQuery();
                if (query == null) break;
                System.out.println("Result: " + vectorialSearch.search(query));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
//        Lab01();
//        Lab02();
//        Lab03();
//        ParallelIndexesCreator();
//        Lab04();
        Project();
    }
}