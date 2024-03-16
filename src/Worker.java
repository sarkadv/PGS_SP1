import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Worker implements Runnable {

    private final int ID;
    private boolean busy;
    private final int workerTime;
    private final int loadingTime;
    private final Semaphore resourceSemaphore;
    private int minedResources;
    private int totalMinedResources;
    private int currentBlockTime;
    private final LorryManager lorryManager;
    private final Printer printer;
    private boolean stopWorking;

    public Worker(int ID, int workerTime, LorryManager lorryManager, Printer printer) {
        this.ID = ID;
        this.busy = false;
        this.workerTime = workerTime;
        this.loadingTime = 10;    // loading to lorry in ms
        this.resourceSemaphore = new Semaphore(0);
        this.minedResources = 0;
        this.totalMinedResources = 0;
        this.currentBlockTime = 0;
        this.lorryManager = lorryManager;
        this.printer = printer;
        this.stopWorking = false;
    }

    @Override
    public void run() {
        while (!stopWorking) {
            work();
        }
    }

    private void work() {
        if (this.resourceSemaphore.tryAcquire(1)) {
            long workTime = ThreadLocalRandom.current().nextLong(workerTime) + 1;
            this.waitForTime(workTime);
            this.currentBlockTime += workTime;
            this.minedResources++;
            this.totalMinedResources++;

            String line = "resource mined in time [ms]: " + workTime;
            printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

            if (this.resourceSemaphore.availablePermits() == 0) {   // the entire block has been mined
                line = "block mined in time [ms]: " + this.currentBlockTime;
                printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

                // load to lorry
                while (this.minedResources > 0) {
                    synchronized (this.lorryManager) {
                        this.waitForTime(this.loadingTime);
                        boolean full = this.lorryManager.loadLorry();
                        this.minedResources--;

                        line = "loaded resource onto lorry";
                        printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

                        if (full) {
                            this.lorryManager.sendOffLorry();
                            this.lorryManager.newLorryInMine();
                        }
                    }
                }

                this.currentBlockTime = 0;
                this.busy = false;
            }
        }
    }

    private void waitForTime(long waitTime) {
        long prevTime = System.currentTimeMillis();

        while (true) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - prevTime >= waitTime) {
                break;
            }
        }
    }

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
