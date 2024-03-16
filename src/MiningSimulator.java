import java.io.File;

/**
 * The main class of the program.
 * Takes care of processing the program arguments and sets off the start of the simulation.
 */
public class MiningSimulator {

    /** file containing the layout of the resources */
    private File inputFile;

    /** file that simulation events will be written to */
    private File outputFile;

    /** number of worker threads that will be created */
    private int workerCount;

    /** the maximum time in milliseconds a worker will mine a resource */
    private int workerTime;

    /** the maximum units of resources that a lorry is able to carry */
    private int lorryCap;

    /** the maximum time in milliseconds a lorry will drive to the ferry (and from the ferry to the goal) */
    private int lorryTime;

    /** the maximum number of lorries that a ferry is able to fit */
    private int ferryCap;

    /**
     * The main method of the program.
     * Takes care of processing the program arguments and sets off the start of the simulation.
     * Creates the shared Printer instance which handles writing to the output file.
     * Creates the Foreman instance which organizes all workers.
     * The program ends after the foreman is done organizing work.
     * @param args  program arguments
     */
    public static void main(String[] args) {
        MiningSimulator sim = new MiningSimulator();
        Foreman foreman;

        if (!sim.parseArguments(args)) {    // wrong program arguments
            System.out.println("Wrong program arguments. Usage: \n" +
                    "java -jar <path to archive> -i <input file> -o <output file> -cWorker\n" +
                    "<int> -tWorker <int> -capLorry <int> -tLorry <int> -capFerry <int>\n");
        }
        else {
            sim.printArguments();

            // printer instance shared among all objects that write to file
            Printer printer = new Printer(sim.outputFile);
            foreman = new Foreman(sim.inputFile, sim.workerCount, sim.workerTime, sim.lorryCap, sim.lorryTime,
                    sim.ferryCap, printer);

            foreman.organizeWork();     // start the simulation
            printer.close();    // printer is closed after all work is done
        }
    }

    /**
     * Method that takes of processing the program arguments.
     * @param args  program arguments
     * @return  true if all argmuments are valid / false otherwise
     */
    private boolean parseArguments(String[] args) {
        File input;
        File output;
        int cWorker;
        int tWorker;
        int capLorry;
        int tLorry;
        int capFerry;

        if (args.length < 14) {
            return false;   // not enough arguments
        }

        if (!args[0].equals("-i")) {
            return false;
        }

        input = new File(args[1]);
        if (!input.exists() || input.isDirectory()) {   // the input file has to exist and cannot be a directory
            System.out.println("Invalid input file.");
            return false;
        }
        else {
            this.inputFile = input;
        }

        if (!args[2].equals("-o")) {
            return false;
        }

        output = new File(args[3]);
        if (output.isDirectory()) {     // the output file cannot be a directory
            return false;
        }
        else {
            this.outputFile = output;
        }

        if (!args[4].equals("-cWorker")) {
            return false;
        }

        // check if argument is a positive integer
        if (!isNumeric(args[5])) {
            return false;
        }
        cWorker = Integer.parseInt(args[5]);
        this.workerCount = cWorker;

        if (!args[6].equals("-tWorker")) {
            return false;
        }

        if (!isNumeric(args[7])) {
            return false;
        }
        tWorker = Integer.parseInt(args[7]);
        this.workerTime = tWorker;

        if (!args[8].equals("-capLorry")) {
            return false;
        }

        if (!isNumeric(args[9])) {
            return false;
        }
        capLorry = Integer.parseInt(args[9]);
        this.lorryCap = capLorry;

        if (!args[10].equals("-tLorry")) {
            return false;
        }

        if (!isNumeric(args[11])) {
            return false;
        }
        tLorry = Integer.parseInt(args[11]);
        this.lorryTime = tLorry;

        if (!args[12].equals("-capFerry")) {
            return false;
        }

        if (!isNumeric(args[13])) {
            return false;
        }
        capFerry = Integer.parseInt(args[13]);
        this.ferryCap = capFerry;

        return true;

    }

    /**
     * Method that prints every program argument to the console.
     */
    private void printArguments() {
        System.out.println("input file: " + this.inputFile);
        System.out.println("output file: " + this.outputFile);
        System.out.println("worker count: " + this.workerCount);
        System.out.println("worker time: " + this.workerTime);
        System.out.println("lorry cap: " + this.lorryCap);
        System.out.println("lorry time: " + this.lorryTime);
        System.out.println("ferry cap: " + this.ferryCap);
        System.out.println("----------------------------------------");

    }

    /**
     * Method decides whether a string represents a positive integer.
     * @param str   string representing a number
     * @return      true if string is a positive integer / false otherwise
     */
    private boolean isNumeric(String str) {
        return str.matches("[0-9]+");
    }
}