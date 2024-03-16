import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class LorryManager {

    private final int lorryCap;
    private final int lorryTime;
    private int lorriesCreated;
    private final int lorriesCreatedMax;
    private Lorry lorryInMine;
    private final Thread[] lorryThreads;
    private final Lorry[] lorries;
    private int resourcesDelivered;
    private final CyclicBarrier ferryBarrier;
    private final Printer printer;

    /* how many resources will be loaded onto the last lorry, so that it does not wait forever */
    private final int lastLorryCap;

    public LorryManager(int lorryCap, int lorryTime, int ferryCap, Printer printer, int allResourcesCount) {
        this.lorryCap = lorryCap;
        this.lorryTime = lorryTime;
        this.lorriesCreated = 0;
        this.lorriesCreatedMax = (int)Math.ceil((double)allResourcesCount / this.lorryCap); // round up to the nearest integer
        this.lorries = new Lorry[this.lorriesCreatedMax];
        this.resourcesDelivered = 0;
        this.lorryThreads = new Thread[this.lorriesCreatedMax];
        this.ferryBarrier = new CyclicBarrier(ferryCap, new Ferry(System.currentTimeMillis(), printer));
        this.printer = printer;
        this.lastLorryCap = allResourcesCount % lorryCap;

        this.newLorryInMine();  // create a lorry waiting in mine
    }

    public synchronized boolean loadLorry() {
        return this.lorryInMine.loadLorry();
    }

    public synchronized void sendOffLorry() {
        this.lorryInMine.sendOffLorry();
    }

    public synchronized void newLorryInMine() {
        if (this.lorriesCreated == this.lorriesCreatedMax) {
            return;
        }

        if (this.lorriesCreated < this.lorriesCreatedMax - 1) {     // not the last created lorry
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, this.lorryCap, this.lorryTime, this.ferryBarrier, this.printer);
        }
        else if (this.lorriesCreated < this.lorriesCreatedMax) {    // the last created lorry - different lorry cap
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, this.lastLorryCap, this.lorryTime, this.ferryBarrier, this.printer);
        }

        Thread lorryThread = new Thread(this.lorryInMine);
        lorryThreads[lorriesCreated] = lorryThread;
        lorries[lorriesCreated] = this.lorryInMine;
        this.lorriesCreated++;
        lorryThread.start();
    }

    public int waitForAllResourcesDelivered() {
        for (int i = 0; i < this.lorriesCreated; i++) {
            try {
                this.lorryThreads[i].join();
                this.resourcesDelivered += this.lorries[i].getLoadedResources();

            } catch (InterruptedException e) {
                System.err.println("Lorry thread " + (i + 1) + " was interrupted.");
            }
        }

        return this.resourcesDelivered;
    }
}
