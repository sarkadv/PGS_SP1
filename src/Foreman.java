import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Foreman {

    private final int ID;
    private Scanner scanner;
    private File inputFile;
    private int workerCount;
    private int workerTime;
    private List<Block> blocksToMine;
    private List<Worker> workers;
    private ThreadGroup workerThreads;
    private Printer printer;

    public Foreman(File inputFile, int workerCount, int workerTime, Printer printer) {
        this.ID = 1;
        this.inputFile = inputFile;
        this.workerCount = workerCount;
        this.workerTime = workerTime;
        this.workers = new ArrayList<Worker>();
        this.workerThreads = new ThreadGroup("workers");
        this.printer = printer;

        analyzeMap();
        createWorkers();
    }

    private void analyzeMap() {
        blocksToMine = new LinkedList<Block>();

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

        String line = "blocks found: " + this.getBlocksCount() + " | total resources found: " + this.getResourceCount();
        printer.printEvent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FOREMAN.toString(), this.ID, line);
    }

    public void organizeWork() {
        while (this.blocksToMine.size() > 0) {
            Worker worker = findFreeWorker();

            if (worker == null) {   // no free workers
                continue;
            }
            else {
                Block block = this.blocksToMine.get(0);
                this.blocksToMine.remove(0);
                worker.setBusy(true);
                worker.addResources(block.getResources());
            }
        }

        while (true) {
            if (this.allWorkersDone()) {
                this.workerThreads.interrupt();
                break;
            }
            else {
                Thread.yield();
            }
        }
    }

    private Worker findFreeWorker() {
        Worker found = null;

        for (int i = 0; i < this.workers.size(); i++) {
            if (!this.workers.get(i).isBusy()) {
                found = this.workers.get(i);
                break;
            }
        }

        return found;
    }

    private boolean allWorkersDone() {
        for (int i = 0; i < this.workers.size(); i++) {
            if (this.workers.get(i).isBusy()) {
                return false;
            }
        }
        return true;
    }

    private void createWorkers() {
        for (int i = 1; i <= workerCount; i++) {
            Worker newWorker = new Worker(i, workerTime, this.printer);
            workers.add(newWorker);
            Thread workerThread = new Thread(workerThreads, newWorker);
            workerThread.start();
        }
    }

    public int getBlocksCount() {
        return this.blocksToMine.size();
    }

    public int getResourceCount() {
        Iterator<Block> iterator = this.blocksToMine.iterator();
        int count = 0;

        while (iterator.hasNext()) {
            count += iterator.next().getResources();
        }

        return count;
    }

}
