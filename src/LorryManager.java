import java.util.ArrayList;
import java.util.List;

public class LorryManager {

    private int lorryCap;
    private int lorriesCreated;
    private int lorriesCreatedMax;
    private Lorry lorryInMine;
    private List<Thread> lorryThreads;
    private int resourcesDelivered;
    private final int allResourcesCount;
    private Printer printer;

    public LorryManager(int lorryCap, Printer printer, int allResourcesCount) {
        this.lorryCap = lorryCap;
        this.lorriesCreated = 0;
        this.lorriesCreatedMax = allResourcesCount / this.lorryCap;
        this.lorryInMine = new Lorry(this.lorriesCreated + 1, lorryCap, printer);
        this.lorriesCreated++;
        this.lorryThreads = new ArrayList<Thread>();
        this.resourcesDelivered = 0;
        this.allResourcesCount = allResourcesCount;
        Thread lorryThread = new Thread(this.lorryInMine);
        lorryThreads.add(lorryThread);
        lorryThread.start();
    }

    public synchronized boolean loadLorry() {
        return this.lorryInMine.loadLorry();
    }

    public synchronized void sendOffLorry() {
        this.lorryInMine.sendOffLorry();
    }

    public synchronized void newLorryInMine() {
        if (this.lorriesCreated < this.lorriesCreatedMax) {
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, lorryCap, printer);
            Thread lorryThread = new Thread(this.lorryInMine);
            lorryThreads.add(lorryThread);
            this.lorriesCreated++;
            lorryThread.start();
        }
    }

    public void waitForAllResourcesDelivered() {
        while (true) {
            for (int i = 0; i < this.lorryThreads.size(); i++) {
                try {
                    this.lorryThreads.get(i).join();
                } catch (InterruptedException e) {
                    this.lorryThreads.remove(i);
                }
            }
            break;
        }
    }
}
