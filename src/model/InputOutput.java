package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class InputOutput {
    /**
     * Read and process file
     *
     * @param filePath path to file
     * @return Object - width, height, living cells
     */
    public static Object[] readFile(String filePath) {
        Charset charset = Charset.forName("UTF-8");
        List<String> allLines = new ArrayList<>();

        // Read file, filter empty lines
        try (Stream<String> stream = Files.lines(Paths.get(filePath), charset).filter(line -> !line.isEmpty())) {
            allLines = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         *  Set width, height of game board accordingly,
         *  remove first and second entry from ArrayList
         */
        int xLength = Integer.parseInt(allLines.get(0).substring(2));
        allLines.remove(0);
        int yLength = Integer.parseInt(allLines.get(0).substring(2));
        allLines.remove(0);

        // Create ArrayList with positions (x,y)
        ArrayList<Integer[]> positions = new ArrayList<>();
        for (int i = 0; i < allLines.size(); i++) {
            positions.add(new Integer[]{Integer.parseInt(allLines.get(i).split(",")[0]), Integer.parseInt(allLines.get(i).split(",")[1])});
        }

        return new Object[]{xLength, yLength, positions};
    }

    /**
     * Show result in console
     *
     * @param model desired generation of world
     */
    public static void printToConsole(Buildable model) {
        boolean[][] tmp = new boolean[model.getYLength()][model.getXLength()];
        for (Integer[] cell : model.getPositions()) {
            tmp[cell[1]][cell[0]] = true;
        }
        for (int y = 0; y < tmp.length; y++) {
            for (int x = 0; x < tmp[0].length; x++) {
                if (tmp[y][x]) {
                    System.out.print("X");
                } else {
                    System.out.print("-");
                }
            }
            System.out.println();
        }
    }

    /**
     * Write board size and living cells to file
     *
     * @param model      desired generation of world
     * @param outputPath specified output path
     * @throws IOException error while writing file
     */
    public static void createFile(Buildable model, String outputPath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
        bw.write("x=" + model.getXLength());
        bw.newLine();
        bw.write("y=" + model.getYLength());
        bw.newLine();

        for (Integer[] cell : model.getPositions()) {
            bw.write(cell[0] + "," + cell[1]);
            bw.newLine();
        }
        bw.close();
    }
}
