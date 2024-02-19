import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Foreman {
    private Scanner scanner;
    private File inputFile;
    private File outputFile;
    private int workerCount;
    private List<Block> blocksToMine;

    public Foreman(File inputFile, File outputFile, int workerCount) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.workerCount = workerCount;

        analyzeMap();
    }

    private void analyzeMap() {
        blocksToMine = new ArrayList<Block>();

        try {
            scanner = new Scanner(this.inputFile);

            while (scanner.hasNext()) {
                String token = scanner.next();
                blocksToMine.add(new Block(token.length()));
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("blocks found: " + this.getBlocksCount());
        System.out.println("total resources found: " + this.getResourceCount());
        System.out.println("----------------------------------------");

    }

    public int getBlocksCount() {
        return this.blocksToMine.size();
    }

    public int getResourceCount() {
        int count = 0;

        for (int i = 0; i < this.blocksToMine.size(); i++) {
            count += this.blocksToMine.get(i).getResources();
        }

        return count;
    }

}
