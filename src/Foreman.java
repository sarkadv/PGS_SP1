import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Foreman {

    private final int ID;
    private List<Block> blocksToMine;
    private final Worker[] workers;
    private final LorryManager lorryManager;
    private final Printer printer;

    public Foreman(File inputFile, int workerCount, int workerTime, int lorryCap, int lorryTime, int ferryCap, Printer printer) {
        this.ID = 1;
        this.workers = new Worker[workerCount];
        this.printer = printer;

        analyzeMap(inputFile);
        this.lorryManager = new LorryManager(lorryCap, lorryTime,  ferryCap, this.printer, this.getResourceCount());
        createWorkers(workerTime);
    }

    private void analyzeMap(File inputFile) {
        blocksToMine = new LinkedList<Block>();

        try {
            Scanner scanner = new Scanner(inputFile);

            while (scanner.hasNext()) {
                String token = scanner.next();
                blocksToMine.add(new Block(token.length()));
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            System.err.println("The input file was not found.");
        }

        String line = "blocks found: " + this.getBlocksCount() + " | total resources found: " + this.getResourceCount();
        printer.printEventToConsoleAndFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FOREMAN.toString(), this.ID, line);
    }

    public void organizeWork() {
        while (this.blocksToMine.size() > 0) {
            Worker worker = findFreeWorker();

            if (worker != null) {   // there is a free worker
                Block block = this.blocksToMine.get(0);
                this.blocksToMine.remove(0);
                worker.setBusy(true);
                worker.addResources(block.getResources());
            }
        }

        this.endWork();
    }

    private void endWork() {
        // wait for all resources to be delivered
        int resourcesDelivered = this.lorryManager.waitForAllResourcesDelivered();  // all lorries at target location
        String line = "total resources delivered to target location: " + resourcesDelivered;
        printer.printEventToConsole(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FOREMAN.toString(), this.ID, line);

        // wait for all workers to end their job
        while (true) {
            if (this.allWorkersDone()) {
                for (int i = 0; i < this.workers.length; i++) {
                    int resourcesMined = this.workers[i].getTotalMinedResources();
                    line = "total resources mined: " + resourcesMined;
                    printer.printEventToConsole(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.workers[i].getID(), line);
                    this.workers[i].setStopWorking(true);
                }
                break;
            }
            else {
                Thread.yield();
            }
        }
    }

    private Worker findFreeWorker() {
        Worker found = null;

        for (int i = 0; i < this.workers.length; i++) {
            if (!this.workers[i].isBusy()) {
                found = this.workers[i];
                break;
            }
        }

        return found;
    }

    private boolean allWorkersDone() {
        for (int i = 0; i < this.workers.length; i++) {
            if (this.workers[i].isBusy()) {
                return false;
            }
        }
        return true;
    }

    private void createWorkers(int workerTime) {
        for (int i = 1; i <= this.workers.length; i++) {
            Worker newWorker = new Worker(i, workerTime, this.lorryManager, this.printer);
            this.workers[i - 1] = newWorker;
            Thread workerThread = new Thread(newWorker);
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
