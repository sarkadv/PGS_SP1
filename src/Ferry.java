import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * There is only one ferry in the simulation.
 * The class only has the run() method, which is exectued when the ferry barrier is broken,
 * as enough lorries have arrived.
 */
public class Ferry implements Runnable {

    /** thread ID will always be 1 because there is only 1 ferry */
    private final int ID;

    /** the last time the ferry has reset indicates the last time the lorry has sailed */
    private long barrierResetTime;

    /** printer instance shared among all objects that write to the output file */
    private final Printer printer;

    /**
     * Creates the ferry instance.
     * @param printer               printer instance for writing to file
     */
    public Ferry(Printer printer) {
        this.ID = 1;    // only 1 ferry
        this.barrierResetTime = System.currentTimeMillis();     // the barrier has been created just now
        this.printer = printer;
    }

    /**
     * Method is exectued when the ferry barrier is broken, as enough lorries have arrived.
     * The sail action is ran at once, having no time duration.
     */
    @Override
    public synchronized void run() {
        // how much time it took for all lorries to arrive
        long waitedUntilLoaded = System.currentTimeMillis() - this.barrierResetTime;

        String line = "on its way, loaded after [ms]: " + waitedUntilLoaded;
        printer.printEventToConsoleAndFile(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FERRY.toString(), this.ID, line);

        this.barrierResetTime = System.currentTimeMillis();     // the ferry has sailed just now
    }
}
