import java.io.File;

public class MiningSimulator {
    private File inputFile;
    private File outputFile;
    private int workerCount;
    private int workerTime;
    private int lorryCap;
    private int lorryTime;
    private int ferryCap;
    public static void main(String[] args) {
        MiningSimulator sim = new MiningSimulator();
        Foreman foreman;
        Lorry lorry;

        if (!sim.parseArguments(args)) {    // wrong program arguments
            System.out.println("Wrong program arguments. Usage: \n" +
                    "java -jar <path to archive> -i <input file> -o <output file> -cWorker\n" +
                    "<int> -tWorker <int> -capLorry <int> -tLorry <int> -capFerry <int>\n");
        }
        else {
            sim.printArguments();
            Printer printer = new Printer(sim.outputFile);
            foreman = new Foreman(sim.inputFile, sim.workerCount, sim.workerTime, printer);
            lorry = new Lorry();
            foreman.organizeWork();
            printer.close();
        }
    }

    private boolean parseArguments(String[] args) {
        File input;
        File output;
        int cWorker;
        int tWorker;
        int capLorry;
        int tLorry;
        int capFerry;

        if (args.length < 14) {
            return false;
        }

        if (!args[0].equals("-i")) {
            return false;
        }

        input = new File(args[1]);
        if (!input.exists() || input.isDirectory()) {
            return false;
        }
        else {
            this.inputFile = input;
        }

        if (!args[2].equals("-o")) {
            return false;
        }

        output = new File(args[3]);
        if (output.isDirectory()) {
            return false;
        }
        else {
            this.outputFile = output;
        }

        if (!args[4].equals("-cWorker")) {
            return false;
        }

        cWorker = Integer.parseInt(args[5]);
        if (cWorker < 0) {
            return false;
        }
        else {
            this.workerCount = cWorker;
        }

        if (!args[6].equals("-tWorker")) {
            return false;
        }

        tWorker = Integer.parseInt(args[7]);
        if (tWorker < 0) {
            return false;
        }
        else {
            this.workerTime = tWorker;
        }

        if (!args[8].equals("-capLorry")) {
            return false;
        }

        capLorry = Integer.parseInt(args[9]);
        if (capLorry < 0) {
            return false;
        }
        else {
            this.lorryCap = capLorry;
        }

        if (!args[10].equals("-tLorry")) {
            return false;
        }

        tLorry = Integer.parseInt(args[11]);
        if (tLorry < 0) {
            return false;
        }
        else {
            this.lorryTime = tLorry;
        }

        if (!args[12].equals("-capFerry")) {
            return false;
        }

        capFerry = Integer.parseInt(args[13]);
        if (capFerry < 0) {
            return false;
        }
        else {
            this.ferryCap = capFerry;
        }

        return true;

    }

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
}