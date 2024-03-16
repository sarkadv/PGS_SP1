import java.util.concurrent.CyclicBarrier;

/**
 * Class for managing the creation and termination of lorries.
 */
public class LorryManager {

    /** the maximum units of resources that a lorry is able to carry */
    private final int lorryCap;

    /** the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal) */
    private final int lorryTime;

    /** the number of lorries that were already created during the simulation */
    private int lorriesCreated;

    /** the maximum number of lorries that will be created during the simulation */
    private final int lorriesCreatedMax;

    /** the lorry currently waiting in the mine to be full */
    private Lorry lorryInMine;

    /** every Lorry thread created during the simulation - for joining at the end */
    private final Thread[] lorryThreads;

    /** every Lorry instance created during the simulation - for getting number of delivered resources at the end */
    private final Lorry[] lorries;

    /** barrier that will open after the number of lorries at the ferry reaches the ferry capacity */
    private final CyclicBarrier ferryBarrier;

    /** printer instance shared among all objects that write to the output file */
    private final Printer printer;

    /* number of resources that will be loaded onto the last lorry, so that it does not wait forever */
    private final int lastLorryCap;

    /**
     * Creates a LorryManager instance.
     * There is only 1 LorryManager in the simulation shared among all workers.
     * @param lorryCap  the maximum units of resources that a lorry is able to carry
     * @param lorryTime the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal)
     * @param ferryCap  the maximum number of lorries that a ferry is able to fit
     * @param printer   printer instance shared among all objects that write to the output file
     * @param allResourcesCount     total number of resouces present in the simulation
     */
    public LorryManager(int lorryCap, int lorryTime, int ferryCap, Printer printer, int allResourcesCount) {
        this.lorryCap = lorryCap;
        this.lorryTime = lorryTime;
        this.lorriesCreated = 0;

        // round up to the nearest integer
        this.lorriesCreatedMax = (int)Math.ceil((double)allResourcesCount / this.lorryCap);

        this.lorries = new Lorry[this.lorriesCreatedMax];
        this.lorryThreads = new Thread[this.lorriesCreatedMax];

        this.ferryBarrier = new CyclicBarrier(ferryCap, new Ferry(printer));    // barrier is shared among all lorries
        this.lastLorryCap = allResourcesCount % lorryCap;

        this.printer = printer;

        this.newLorryInMine();  // create the first lorry waiting in mine
    }

    /**
     * Method called by a worker to load 1 resource onto the lorry.
     * @return  true if lorry is full / false otherwise
     */
    public synchronized boolean loadLorry() {
        return this.lorryInMine.loadLorry();
    }

    /**
     * Method called by a worker to signal the lorry to go.
     */
    public synchronized void sendOffLorry() {
        this.lorryInMine.sendOffLorry();
    }

    /**
     * Method creates a new Lorry instance in the mine, after the previous lorry has reached its capacity and has left.
     * It is called by a worker after they send off the previous lorry.
     */
    public synchronized void newLorryInMine() {
        // to prevent a new lorry from being created when it will not be filled
        if (this.lorriesCreated == this.lorriesCreatedMax) {
            return;
        }

        if (this.lorriesCreated < this.lorriesCreatedMax - 1) {     // not the last created lorry
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, this.lorryCap, this.lorryTime,
                    this.ferryBarrier, this.printer);
        }
        else if (this.lorriesCreated < this.lorriesCreatedMax) {    // the last created lorry - different lorry cap
            this.lorryInMine = new Lorry(this.lorriesCreated + 1, this.lastLorryCap, this.lorryTime,
                    this.ferryBarrier, this.printer);
        }

        Thread lorryThread = new Thread(this.lorryInMine);
        lorryThreads[lorriesCreated] = lorryThread;
        lorries[lorriesCreated] = this.lorryInMine;

        this.lorriesCreated++;

        lorryThread.start();
    }

    /**
     * Method waits until all lorries are at the target location (their run() methods have ended).
     * Called by the Foreman instance after it has no more blocks to assign.
     * From every lorry, the amount of delivered resources is added to the sum.
     * @return  sum of all delivered resources by all lorries
     */
    public int waitForAllResourcesDelivered() {
        int resourcesDelivered = 0;

        for (int i = 0; i < this.lorriesCreatedMax; i++) {
            try {
                this.lorryThreads[i].join();
                resourcesDelivered += this.lorries[i].getLoadedResources();

            } catch (InterruptedException e) {
                System.err.println("Lorry thread " + (i + 1) + " was interrupted.");
            }
        }

        return resourcesDelivered;
    }
}
