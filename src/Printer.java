import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that handles mainly writing to the given output file, but also to the console.
 */
public class Printer {

    /** for writing to the file */
    private FileWriter fileWriter;

    private final String delimiter;

    /**
     * Creates a Printer instance.
     * This instance will be shared by every object in the program that needs to write to the output file.
     * @param outputFile    given output file
     */
    Printer(File outputFile) {
        try {
            this.fileWriter = new FileWriter(outputFile);
        }
        catch (IOException e) {
            System.err.println("There was an error writing to the output file.");
        }

        this.delimiter = " | ";
    }

    /**
     * Method prints a description of an event to the output file.
     * @param timestamp     time of the event
     * @param role          role of the thread that caused the event
     * @param threadID      ID of the thread that caused the event
     * @param description   description of the event
     */
    public synchronized void printEventToFile(String timestamp, String role, long threadID, String description) {
        String line = timestamp + this.delimiter + role + this.delimiter + threadID
                + this.delimiter + description + "\n";

        try {
            this.fileWriter.write(line);
        } catch (IOException e) {
            System.err.println("There was an error writing to the output file.");
        }
    }

    /**
     * Method prints a description of an event to the console.
     * @param timestamp     time of the event
     * @param role          role of the thread that caused the event
     * @param threadID      ID of the thread that caused the event
     * @param description   description of the event
     */
    public synchronized void printEventToConsole(String timestamp, String role, long threadID, String description) {
        String line = timestamp + this.delimiter + role + this.delimiter + threadID
                + this.delimiter + description + "\n";

        System.out.print(line);
    }

    /**
     * Method prints a description of an event both to the console and the output file.
     * @param timestamp     time of the event
     * @param role          role of the thread that caused the event
     * @param threadID      ID of the thread that caused the event
     * @param description   description of the event
     */
    public synchronized void printEventToConsoleAndFile(String timestamp, String role, long threadID, String description) {
        this.printEventToFile(timestamp, role, threadID, description);
        this.printEventToConsole(timestamp, role, threadID, description);
    }

    /**
     * Method closes the output file for writing.
     */
    public void close() {
        try {
            this.fileWriter.flush();    // flush the buffer
            this.fileWriter.close();
        } catch (IOException e) {
            System.err.println("There was an error closing the output file.");
        }
    }
}
