import controller.CLI;
import controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import model.BooleanModel;
import model.Buildable;

import java.io.IOException;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class GameOfLife extends Application {

    /**
     * Main (launches CLI or UI)
     *
     * @param args optional arguments start CLI
     */
    public static void main(String[] args) {
        if (args.length != 0 || args == null) {
            try {
                CLI.startCLI(args);
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            launch(args);
            System.exit(0);
        }
    }

    /**
     * Start Application
     *
     * @param primaryStage stage
     * @throws Exception handles exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Buildable world = new BooleanModel(100, 50);
        new Controller(primaryStage, world);
    }
}
