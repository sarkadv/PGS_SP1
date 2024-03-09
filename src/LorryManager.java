import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class LorryManager {

    private int lorryCap;
    private int lorryTime;
    private int lorriesCreated;
    private int lorriesCreatedMax;
    private Lorry lorryInMine;
    private Thread[] lorryThreads;
    private int resourcesDelivered;
    private CyclicBarrier ferryBarrier;
    private Printer printer;

    public LorryManager(int lorryCap, int lorryTime, int ferryCap, Printer printer, int allResourcesCount) {
        this.lorryCap = lorryCap;
        this.lorryTime = lorryTime;
        this.lorriesCreated = 0;
        this.lorriesCreatedMax = allResourcesCount / this.lorryCap;
        this.resourcesDelivered = 0;
        this.lorryThreads = new Thread[this.lorriesCreatedMax];
        this.ferryBarrier = new CyclicBarrier(ferryCap, new Ferry(System.currentTimeMillis(), printer));
        this.printer = printer;

        this.newLorryInMine();  // create a lorry waiting in mine
    }

    public synchronized boolean loadLorry() {
        return this.lorryInMine.loadLorry();
    }

    public synchronized void sendOffLorry() {
        this.lorryInMine.sendOffLorry();
    }

    public synchronized void newLorryInMine() {
        if (this.lorriesCreated < this.lorriesCreatedMax) {
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, this.lorryCap, this.lorryTime, this.ferryBarrier, this.printer);
            Thread lorryThread = new Thread(this.lorryInMine);
            lorryThreads[lorriesCreated] = lorryThread;
            this.lorriesCreated++;
            lorryThread.start();
        }
    }

    public int waitForAllResourcesDelivered() {
        while (true) {
            for (int i = 0; i < this.lorryThreads.length; i++) {
                try {
                    this.lorryThreads[i].join();
                    this.resourcesDelivered += this.lorryCap;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        return this.resourcesDelivered;
    }
}
