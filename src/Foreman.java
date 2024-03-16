import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class represents a foreman in the mine.
 * There is only one foreman in the simulation.
 * The foreman takes care of identifying all the resources and sharing the work between worker threads.
 * The foreman also decides when all the work is done and ends the simulation.
 */
public class Foreman {

    /** thread ID will always be 1 because there is only 1 foreman */
    private final int ID;

    /** a list of blocks the foreman has identified in the input file */
    private final List<Block> blocksToMine;

    /** how many single resources have been found in the input file and should be delivered */
    private int resourcesToDeliver;

    /** worker instances to be able to get data from workers */
    private final Worker[] workers;

    /** the same lorryManager instance will be passed to all the workers */
    private final LorryManager lorryManager;

    /** printer instance shared among all objects that write to the output file */
    private final Printer printer;

    /**
     * When a foreman instance is created, the map is immediatelly analyzed and a given number of workers is created.
     * @param inputFile file containing the layout of the resources
     * @param workerCount   number of worker threads that will be created
     * @param workerTime    the maximum time in milliseconds a worker will mine a resource
     * @param lorryCap  the maximum units of resources that a lorry is able to carry
     * @param lorryTime the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal)
     * @param ferryCap  the maximum number of lorries that a ferry is able to fit
     * @param printer   printer instance for writing to file
     */
    public Foreman(File inputFile, int workerCount, int workerTime, int lorryCap, int lorryTime,
                   int ferryCap, Printer printer) {
        this.ID = 1;    // only 1 foreman
        this.printer = printer;

        this.blocksToMine = analyzeMap(inputFile);
        this.lorryManager = new LorryManager(lorryCap, lorryTime,  ferryCap, this.printer, this.resourcesToDeliver);
        this.workers = createWorkers(workerCount, workerTime);
    }

    /**
     * Method analyzes the given input file.
     * Every token found in the file represents a block of resources.
     * @param inputFile     file containing the layout of the resources
     * @return              list of every block found in the file
     */
    private List<Block> analyzeMap(File inputFile) {
        List<Block> blocksToMine = new LinkedList<Block>();
        int resourcesFound = 0;

        try {
            Scanner scanner = new Scanner(inputFile);

            while (scanner.hasNext()) {
                String token = scanner.next();

                // the length of the token determines the number of resources inside the block
                blocksToMine.add(new Block(token.length()));
                resourcesFound += token.length();
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            System.err.println("The input file was not found.");
        }

        this.resourcesToDeliver = resourcesFound;

        // print information about found resources
        String line = "blocks found: " + blocksToMine.size() + " | total resources found: " + this.resourcesToDeliver;
        printer.printEventToConsoleAndFile(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FOREMAN.toString(), this.ID, line);

        return blocksToMine;
    }

    /**
     * The foreman assigns blocks to mine to non-busy worker threads.
     * After all the blocks have been assigned, the foreman waits until all work is done.
     */
    public void organizeWork() {
        while (this.blocksToMine.size() > 0) {
            Worker worker = findFreeWorker();

            if (worker != null) {   // there is a free worker
                Block block = this.blocksToMine.get(0);
                this.blocksToMine.remove(0);
                worker.setBusy(true);
                worker.addResources(block.getResources());  // the worker has been given a signal to work
            }
        }

        this.endWork();     // no more blocks, the foreman waits
    }

    /**
     * The foreman waits for all resources to be delivered.
     * After this, all the workers can be interrupted and the simulation can end.
     */
    private void endWork() {
        // wait for all resources to be delivered
        int resourcesDelivered = this.lorryManager.waitForAllResourcesDelivered();  // all lorries at target location

        // print lorry stats
        String line = "total resources delivered to target location: " + resourcesDelivered;
        printer.printEventToConsole(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FOREMAN.toString(), this.ID, line);

        for (int i = 0; i < this.workers.length; i++) {
            int resourcesMined = this.workers[i].getTotalMinedResources();

            // print worker stats
            line = "total resources mined: " + resourcesMined;
            printer.printEventToConsole(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                    Roles.WORKER.toString(), this.workers[i].getID(), line);

            this.workers[i].setStopWorking(true);   // worker thread will end
        }
    }

    /**
     * Method finds a worker that is not busy (= they are not mining or loading resources).
     * @return      a free worker / NULL if all are busy
     */
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

    /**
     * Method creates a given number of Worker instances and worker threads.
     * The threads are started but are waiting for the foreman to give them resources to mine.
     * @param workerCount   number of worker threads and instances that will be created
     * @param workerTime    the maximum time in milliseconds a worker will mine a resource
     * @return              array of Worker instances of the given size
     */
    private Worker[] createWorkers(int workerCount, int workerTime) {
        Worker[] workers = new Worker[workerCount];

        for (int i = 1; i <= workerCount; i++) {
            Worker newWorker = new Worker(i, workerTime, this.lorryManager, this.printer);
            workers[i - 1] = newWorker;
            Thread workerThread = new Thread(newWorker);
            workerThread.start();
        }

        return workers;
    }

}
