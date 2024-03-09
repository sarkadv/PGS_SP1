import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Worker implements Runnable {

    private final int ID;
    private boolean busy;
    private int workerTime;
    private int loadingTime;
    private Semaphore semaphore;
    private int minedResources;
    private int totalMinedResources;
    private int currentBlockTime;
    private LorryManager lorryManager;
    private Printer printer;

    public Worker(int ID, int workerTime, LorryManager lorryManager, Printer printer) {
        this.ID = ID;
        this.busy = false;
        this.workerTime = workerTime;
        this.loadingTime = 1000;    // loading to lorry in ms
        this.semaphore = new Semaphore(0);
        this.minedResources = 0;
        this.totalMinedResources = 0;
        this.currentBlockTime = 0;
        this.lorryManager = lorryManager;
        this.printer = printer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                work();
            }
        } catch (InterruptedException e) {
            // run method ends
        }
    }

    private void work() throws InterruptedException {
        this.semaphore.acquire(1);
        long workTime = ThreadLocalRandom.current().nextLong(workerTime) + 1;
        this.waitForTime(workTime);
        this.currentBlockTime += workTime;
        this.minedResources++;
        this.totalMinedResources++;

        String line = "resource mined in time [ms]: " + workTime;
        printer.printEvent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

        if (this.semaphore.availablePermits() == 0) {   // the entire block has been mined
            line = "block mined in time [ms]: " + this.currentBlockTime;
            printer.printEvent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

            // load to lorry
            while (this.minedResources > 0) {
                synchronized (this.lorryManager) {
                    this.waitForTime(this.loadingTime);
                    boolean full = this.lorryManager.loadLorry();
                    this.minedResources--;

                    line = "loaded resource onto lorry";
                    printer.printEvent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.WORKER.toString(), this.ID, line);

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
        this.semaphore.release(resources);
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
}
