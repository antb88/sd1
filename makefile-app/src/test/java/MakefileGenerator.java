import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ant on 5/9/16.
 */
public class MakefileGenerator {


    private Integer nextId = 1;
    private File file;
    private BufferedWriter writer;

    private Integer getNextId() {
        return nextId++;
    }

    public void createTreeFile(String name, String rootName, int treeHeight) throws IOException {
        this.file = new File(name);
        this.writer = new BufferedWriter(new FileWriter(file));
        createTreeFile(rootName, treeHeight);
        writer.flush();
    }

    private void createTreeFile(String rootName, int treeHeight) {
        if (treeHeight == 0) {
            return;
        }
        String task1 = "task" + getNextId();
        String task2 = "task" + getNextId();
        writeToFile(task1 + " = " + rootName + "\n");
        writeToFile(task2 + " = " + rootName + "\n");
        createTreeFile(task1, treeHeight - 1);
        createTreeFile(task2, treeHeight - 1);
    }

    private void writeToFile(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MakefileGenerator m = new MakefileGenerator();
        m.createTreeFile("tree_build.txt", "file", 14);
    }
}
