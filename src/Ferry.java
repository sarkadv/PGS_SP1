import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ferry implements Runnable {

    private final int ID;
    private long barrierCreatedTime;
    private final Printer printer;

    public Ferry(long barrierCreatedTime, Printer printer) {
        this.ID = 1;
        this.barrierCreatedTime = barrierCreatedTime;
        this.printer = printer;
    }

    @Override
    public synchronized void run() {
        long waitedUntilLoaded = System.currentTimeMillis() - this.barrierCreatedTime;
        String line = "on its way, loaded after [ms]: " + waitedUntilLoaded;
        printer.printEventToConsoleAndFile(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Roles.FERRY.toString(), this.ID, line);

        this.barrierCreatedTime = System.currentTimeMillis();
    }
}
