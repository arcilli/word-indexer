package text.directory.explorer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DirectoryExplorer {
    private Queue<File> directoryQueue = new LinkedList<>();

    public DirectoryExplorer(String directoryPath) {
        directoryQueue.add(new File(directoryPath));
    }

    public List<File> explore() {
        List<File> fileList = new LinkedList<>();
        while (!directoryQueue.isEmpty()) {
            File directory = directoryQueue.remove();
            File[] filesFromDirectory = directory.listFiles();
            if (null != filesFromDirectory) {
                for (File file : filesFromDirectory) {
                    if (file.isDirectory()) {
                        directoryQueue.add(file);
                    } else {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }
}
