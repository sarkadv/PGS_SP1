import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class Lorry implements Runnable {

    private final int ID;
    private int lorryCap;
    private int lorryTime;
    private CountDownLatch loadedLatch;
    private CountDownLatch fullSignal;
    private CyclicBarrier ferryBarrier;
    private Printer printer;
    private long instanceCreatedTime;

    public Lorry(int ID, int lorryCap, int lorryTime, CyclicBarrier ferryBarrier, Printer printer) {
        this.ID = ID;
        this.lorryCap = lorryCap;
        this.lorryTime = lorryTime;
        this.ferryBarrier = ferryBarrier;
        this.loadedLatch = new CountDownLatch(this.lorryCap);
        this.fullSignal = new CountDownLatch(1);
        this.printer = printer;
        this.instanceCreatedTime = System.currentTimeMillis();
    }

    public synchronized boolean loadLorry() {
        this.loadedLatch.countDown();
        return this.loadedLatch.getCount() == 0;    // returns true if lorry is full
    }

    public synchronized void sendOffLorry() {
        this.fullSignal.countDown();
    }

    @Override
    public void run() {
        try {
            this.waitUntilLoaded();
            this.fullSignal.await();    // wait until worker sends lorry off
            this.driveToFerry();
            this.ferryBarrier.await();  // wait for ferry cap
            this.driveToTarget();

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void waitUntilLoaded() throws InterruptedException {
        this.loadedLatch.await();   // wait until full
        long waitedUntilLoaded = System.currentTimeMillis() - this.instanceCreatedTime;
        String line = "loaded after [ms]: " + waitedUntilLoaded;
        printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
    }

    private void driveToFerry() {
        long driveTime = ThreadLocalRandom.current().nextLong(this.lorryTime) + 1;
        this.waitForTime(driveTime);    // drive to ferry
        String line = "drove to ferry in time [ms]: " + driveTime;
        printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
    }

    private void driveToTarget() {
        long driveTime = ThreadLocalRandom.current().nextLong(this.lorryTime) + 1;
        this.waitForTime(driveTime);    // drive to goal
        String line = "drove to target location in time [ms]: " + driveTime;
        printer.printEventToFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.LORRY.toString(), this.ID, line);
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
}
