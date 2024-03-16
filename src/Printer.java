import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {
    private FileWriter fileWriter;
    Printer(File outputFile) {
        try {
            this.fileWriter = new FileWriter(outputFile);
        }
        catch (IOException e) {
            System.err.println("There was an error writing to the output file.");
        }
    }

    public synchronized void printEventToFile(String timestamp, String role, long threadID, String description) {
        String line = timestamp + " | " + role + " | " + threadID + " | " + description + "\n";

        try {
            this.fileWriter.write(line);
        } catch (IOException e) {
            System.err.println("There was an error writing to the output file.");
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
            System.err.println("There was an error closing the output file.");
        }
    }
}
