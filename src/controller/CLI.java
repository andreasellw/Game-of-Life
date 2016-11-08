package controller;

import model.BitSetModel;
import model.BooleanModel;
import model.Buildable;
import model.InputOutput;

import java.io.IOException;
import java.util.ArrayList;

import static model.InputOutput.printToConsole;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class CLI {

    /**
     * Process command line input, calculate data, show result in console and write file with output
     *
     * @param args program arguments
     * @throws IOException program arguments don't meet requirements
     */
    public static void startCLI(String[] args) throws IOException {
        Buildable world;
        // Checks if a program argument is missing
        if (args.length != 5) {
            System.out.println("ARG0: filepath (.txt)");
            System.out.println("ARG1: torus (boolean - true = Torus, false = Hard limit");
            System.out.println("ARG2: generation (int)");
            System.out.println("ARG3: output path (.txt)");
            System.out.println("ARG4: model (boolean - true = Boolean[][] Model, false = BitSet Model)");
            System.out.println();
            System.exit(0);
        }
        // Initialize variables with program arguments
        String filePath = args[0];
        boolean torus = Boolean.parseBoolean(args[1]);
        int generation = Integer.parseInt(args[2]);
        String outputPath = args[3];
        boolean model = Boolean.parseBoolean(args[4]);

        // Read file
        Object[] data = InputOutput.readFile(filePath);

        // Initialize variables with processed data from file
        int width = (int) data[0];
        int length = (int) data[1];
        ArrayList<Integer[]> positions = (ArrayList<Integer[]>) data[2];

        // Create world
        if (model) {
            world = new BooleanModel(width, length, positions);
        } else {
            world = new BitSetModel(width, length, positions);
        }
        System.out.println();

        // Set surface mode
        world.setTorus(torus);

        // Develop world to generation x
        for (int i = 0; i < generation; i++) {
            world.developGeneration();
        }

        // Show result in console
        printToConsole(world);

        // Write board size and living cells to file
        InputOutput.createFile(world, outputPath);

        System.out.println();
        System.out.println("ARG0: " + filePath);
        System.out.println("      filepath (.txt)");
        System.out.println("ARG1: " + torus);
        System.out.println("      torus (boolean - true = Torus, false = Hard limit)");
        System.out.println("ARG2  " + generation);
        System.out.println("      generation (int)");
        System.out.println("ARG3  " + outputPath);
        System.out.println("      output path (.txt)");
        System.out.println("ARG4: " + model);
        System.out.println("      model (boolean - true = Boolean[][] Model, false = BitSet Model)");
    }
}
