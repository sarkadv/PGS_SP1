import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class represents a worker that mines blocks assigned by the Foreman, then loads resources onto the lorry.
 */
public class Worker implements Runnable {

    /** depends on the order when the worker was created */
    private final int ID;

    /** true if worker is currently mining or loading resources / false otherwise */
    private boolean busy;

    /** the maximum time in milliseconds a worker will mine a resource */
    private final int workerTime;

    /** time in milliseconds a worker will load a resource onto the lorry */
    private final int loadingTime;

    /** represents the amount of resources that the worker has available to mine, semaphore is raised by Foreman */
    private final Semaphore resourceSemaphore;

    /** number of resources the worker is currently carrying and has not loaded onto the lorry yet */
    private int currentlyCarriedResources;

    /** total amount of resources the worker has mined during the simulation */
    private int totalMinedResources;

    /** time in milliseconds the worker has already spent mining the current block */
    private int currentBlockTime;

    /** wrapper class for loading lorries shared among all workers */
    private final LorryManager lorryManager;

    /** printer instance shared among all objects that write to the output file */
    private final Printer printer;

    /** signal for the thread to end, value is changed by Foreman */
    private boolean stopWorking;

    /**
     * Creates a worker instance.
     * @param ID            depends on the order when the worker was created
     * @param workerTime    the maximum time in milliseconds a worker will mine a resource
     * @param lorryManager  wrapper class for loading lorries shared among all workers
     * @param printer       printer instance shared among all objects that write to the output file
     */
    public Worker(int ID, int workerTime, LorryManager lorryManager, Printer printer) {
        this.ID = ID;
        this.busy = false;
        this.workerTime = workerTime;
        this.loadingTime = 10;
        this.resourceSemaphore = new Semaphore(0);  // worker has no resources to mine in the beginning
        this.currentlyCarriedResources = 0;
        this.totalMinedResources = 0;
        this.currentBlockTime = 0;
        this.lorryManager = lorryManager;
        this.printer = printer;
        this.stopWorking = false;
    }

    /**
     * Method runs until the Foreman has changed the value of this.stopWorking to true, which ends the worker thread.
     */
    @Override
    public void run() {
        while (!this.stopWorking) {
            work();
        }
    }

    /**
     * Method is called in a loop until the Foreman gives the signal to stop working.
     * At first, the worker checks if they have been assigned resources.
     * If so, the worker starts mining.
     * After the entire block has been mined, the worker loads the resources onto the lorry.
     * Then, the worker becomes available to be assigned another block.
     */
    private void work() {
        if (this.resourceSemaphore.tryAcquire(1)) {     // check if Foreman has assigned any resources to mine
            long workTime = ThreadLocalRandom.current().nextLong(workerTime) + 1;
            this.waitForTime(workTime);     // mining
            this.currentBlockTime += workTime;
            this.currentlyCarriedResources++;
            this.totalMinedResources++;

            // print event
            String line = "resource mined in time [ms]: " + workTime;
            printer.printEventToFile(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

            if (this.resourceSemaphore.availablePermits() > 0) {    // there are more resources in the current block
                return;
            }
            else {   // the entire block has been mined
                // print event
                line = "block mined in time [ms]: " + this.currentBlockTime;
                printer.printEventToFile(LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                        Roles.WORKER.toString(), this.ID, line);

                this.loadToLorry();

                // reset worker
                this.currentBlockTime = 0;
                this.busy = false;
            }
        }
    }

    /**
     * Worker loads all currently carried resources onto the lorry waiting in the mine.
     * Only 1 worker can be loading onto the lorry at any given time.
     */
    private void loadToLorry() {
        while (this.currentlyCarriedResources > 0) {

            synchronized (this.lorryManager) {      // only 1 worker can run on the LorryManager instance simultaneously
                this.waitForTime(this.loadingTime);
                boolean full = this.lorryManager.loadLorry();
                this.currentlyCarriedResources--;

                // print event
                String line = "loaded resource onto lorry";
                printer.printEventToFile(LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                                Roles.WORKER.toString(), this.ID, line);

                // lorry is full and will be given a signal to go
                // lorry is replaced by a new one
                if (full) {
                    this.lorryManager.sendOffLorry();
                    this.lorryManager.newLorryInMine();
                }
            }
        }
    }

    /**
     * Method actively waits for a given amount of time.
     * @param waitTime  time to wait in milliseconds.
     */
    private void waitForTime(long waitTime) {
        long prevTime = System.currentTimeMillis();

        while (true) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - prevTime >= waitTime) {
                break;
            }
        }
    }

    /**
     * Method called by the Foreman when it assigns a block for mining to this worker.
     * The resource semaphore is raised by a number of resources, which gives the worker a signal to start mining.
     * @param resources     number of resources contained in the block to mine
     */
    public void addResources(int resources) {
        this.resourceSemaphore.release(resources);
    }

    public int getID() {
        return this.ID;
    }
    public boolean isBusy() {
        return this.busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public int getTotalMinedResources() {
        return this.totalMinedResources;
    }

    public void setStopWorking(boolean stopWorking) {
        this.stopWorking = stopWorking;
    }
}
