package parallelization;

import lombok.extern.java.Log;
import text.directory.explorer.DirectoryExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
public class Master {
    private DirectoryExplorer directoryExplorer;

    public Master(String directoryPath) {
        directoryExplorer = new DirectoryExplorer(directoryPath);
    }

    public void run() {
        // Explore input directory.
        List<File> fileList = directoryExplorer.explore();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            for (File file : new ArrayList<>(fileList)) {
                executor.execute(new Worker(file.toString(), IndexType.DIRECT));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                ;
            }
            log.info("Done with direct indexes.");
        } catch (Exception e) {
            e.printStackTrace();
            executor.shutdown();
        }

//        Work on reverse index.
        executor = Executors.newFixedThreadPool(10);
        try {
            for (File file : new ArrayList<>(fileList)) {
                executor.execute(new Worker(file.toString(), IndexType.REVERSE));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                ;
            }
            log.info("Done with indexes. :)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
