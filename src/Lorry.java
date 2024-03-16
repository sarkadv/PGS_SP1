import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The lorry instance first has to get loaded by workers.
 * After reaching its cap, it travels to the ferry.
 * There, it waits until the ferry is full.
 * After getting off the ferry, it drives to the goal location where it ends.
 */
public class Lorry implements Runnable {

    /** depends on the order when the lorry was created */
    private final int ID;

    /** the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal) */
    private final int lorryTime;

    /** latch that will open after the lorry reaches its capacity */
    private final CountDownLatch loadedLatch;

    /** number of resources that are currently loaded onto the lorry */
    private int loadedResources;

    /** latch that will open after a worker gives the lorry a signal to go */
    private final CountDownLatch fullSignal;

    /** barrier that will open after the number of lorries at the ferry reaches the ferry capacity */
    private final CyclicBarrier ferryBarrier;

    /** printer instance shared among all objects that write to the output file */
    private final Printer printer;

    /** time when this instance was created in milliseconds */
    private final long instanceCreatedTime;

    /**
     * Creates a Lorry instance.
     * @param ID            depends on the order when the lorry was created - counted by LorryManager
     * @param lorryCap      the maximum units of resources that a lorry is able to carry
     * @param lorryTime     the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal)
     * @param ferryBarrier  barrier that will open after the number of lorries at the ferry reaches the ferry capacity
     * @param printer       printer instance shared among all objects that write to the output file
     */
    public Lorry(int ID, int lorryCap, int lorryTime, CyclicBarrier ferryBarrier, Printer printer) {
        this.ID = ID;
        this.lorryTime = lorryTime;
        this.ferryBarrier = ferryBarrier;
        this.loadedLatch = new CountDownLatch(lorryCap);
        this.loadedResources = 0;
        this.fullSignal = new CountDownLatch(1);    // only 1 worker has to signal the lorry to go
        this.printer = printer;
        this.instanceCreatedTime = System.currentTimeMillis();
    }

    /**
     * Method called by LorryManager to load 1 resource onto the lorry waiting in the mine.
     * @return  true if lorry is full / false otherwise
     */
    public synchronized boolean loadLorry() {
        this.loadedResources++;
        this.loadedLatch.countDown();

        return this.loadedLatch.getCount() == 0;
    }

    /**
     * Method called by LorryManager to signal the lorry waiting in the mine to go.
     */
    public synchronized void sendOffLorry() {
        this.fullSignal.countDown();
    }

    /**
     * Method represents the entire lifecycle of a Lorry thread.
     * The Lorry thread ends after it has reached its goal location.
     * Lorry threads are joined in the LorryManager, where they share the number of resources delivered.
     */
    @Override
    public void run() {
        try {
            this.waitUntilLoaded();     // wait until the lorry has reached its capacity of resources
            this.fullSignal.await();    // wait until worker sends lorry off

        } catch (InterruptedException e) {
            System.err.println("Lorry thread " + this.ID + " was interrupted.");
        }

        this.driveToFerry();

        try {
            this.ferryBarrier.await();  // wait until the ferry has reached its capacity of lorries

        } catch (InterruptedException e) {
            System.err.println("Lorry thread " + this.ID + " was interrupted.");
        }
        catch (BrokenBarrierException e) {
            System.err.println("The barrier of lorry thread " + this.ID + " was broken.");
        }

        this.driveToTarget();
    }

    /**
     * Method waits until the lorry has reached its capacity of resources.
     * The fullness of the lorry is represented by a latch.
     * @throws InterruptedException     lorry thread has been interrupted by another thread while waiting
     */
    private void waitUntilLoaded() throws InterruptedException {
        this.loadedLatch.await();   // wait until full

        // print event
        long waitedUntilLoaded = System.currentTimeMillis() - this.instanceCreatedTime;
        String line = "loaded after [ms]: " + waitedUntilLoaded;
        printer.printEventToFile(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
    }

    /**
     * Method waits for a random time between 1 and this.lorryTime.
     * This simulates the lorry driving to the ferry.
     */
    private void driveToFerry() {
        long driveTime = ThreadLocalRandom.current().nextLong(this.lorryTime) + 1;
        this.waitForTime(driveTime);    // drive to ferry

        // print event
        String line = "drove to ferry in time [ms]: " + driveTime;
        printer.printEventToFile(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
    }

    /**
     * Method waits for a random time between 1 and this.lorryTime.
     * This simulates the lorry driving to the goal location.
     */
    private void driveToTarget() {
        long driveTime = ThreadLocalRandom.current().nextLong(this.lorryTime) + 1;
        this.waitForTime(driveTime);    // drive to goal

        // print event
        String line = "drove to target location in time [ms]: " + driveTime;
        printer.printEventToFile(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
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

    public int getLoadedResources() {
        return this.loadedResources;
    }
}
