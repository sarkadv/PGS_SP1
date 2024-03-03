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

    public synchronized void printEvent(String timestamp, String role, long threadID, String description) {
        String line = timestamp + " | " + role + " | " + threadID + " | " + description + "\n";

        // to console
        System.out.print(line);

        // to file
        try {
            this.fileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
