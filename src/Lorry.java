import java.util.concurrent.CountDownLatch;

public class Lorry implements Runnable {

    private final int ID;
    private int lorryCap;
    private CountDownLatch loadedLatch;
    private CountDownLatch fullSignal;
    private Printer printer;

    public Lorry(int ID, int lorryCap, Printer printer) {
        this.ID = ID;
        this.lorryCap = lorryCap;
        this.loadedLatch = new CountDownLatch(this.lorryCap);
        this.fullSignal = new CountDownLatch(1);
        this.printer = printer;
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
            this.loadedLatch.await();
            this.fullSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
