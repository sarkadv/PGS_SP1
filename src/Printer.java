import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {
    private File outputFile;
    private FileWriter fileWriter;
    Printer(File outputFile) {
        this.outputFile = outputFile;

        try {
            this.fileWriter = new FileWriter(outputFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void printEventToFile(String timestamp, String role, long threadID, String description) {
        String line = timestamp + " | " + role + " | " + threadID + " | " + description + "\n";

        try {
            this.fileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void printEventToConsole(String timestamp, String role, long threadID, String description) {
        String line = timestamp + " | " + role + " | " + threadID + " | " + description + "\n";

        System.out.print(line);
    }

    public synchronized void printEventToConsoleAndFile(String timestamp, String role, long threadID, String description) {
        this.printEventToFile(timestamp, role, threadID, description);
        this.printEventToConsole(timestamp, role, threadID, description);
    }

    public void close() {
        try {
            this.fileWriter.flush();
            this.fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
