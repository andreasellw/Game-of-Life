package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.BitSetModel;
import model.BooleanModel;
import model.Buildable;
import model.InputOutput;
import view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class Controller implements EventHandler {
    Stage stage;
    Buildable world;
    private View view;

    /**
     * Constructor
     *
     * @param stage instance of Stage
     * @param world instance of Buildable
     */
    public Controller(Stage stage, Buildable world) {
        this.stage = stage;
        this.world = world;
        this.view = new View(this);
        view.cellSize = 7;
        view.torus = new SimpleBooleanProperty(false);
        view.sceneWidth = world.getXLength() * view.cellSize;
        view.sceneHeight = world.getYLength() * view.cellSize;
        this.world.addObserver(this.view);
        try {
            view.start(stage);
            view.resizeStage();
            view.resizeCanvas(this.world.getXLength(), this.world.getYLength());
            view.refresh(this.world.getPositions());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Duration of generations
        view.timeLine = new Timeline(new KeyFrame(Duration.millis(view.speed), ae -> {
            this.world.developGeneration();
            view.currentGeneration.set(view.currentGeneration.intValue() + 1);
            view.aliveCells.set(this.world.getPositions().size());
        }));
//        view.timeLine.setCycleCount(view.cycleCount);
        view.timeLine.setRate(1.0);
        // Setting generation speed, bound to Slider.
        view.timeLine.rateProperty().addListener((observable, oldValue, newValue) -> {
            view.timeLine.setRate((double) newValue);
        });
        // Reset Run Button
        view.timeLine.setOnFinished(event -> {
            view.bRun.getStyleClass().removeAll("button", "bRun", "bPause");
            view.bRun.getStyleClass().add("button");
            view.bRun.getStyleClass().add("bRun");
            view.bRun.setTooltip(new Tooltip("Run"));
//            System.out.println(view.bRun.getStyleClass()+ " styleclass after setOnFinished.")
            view.isPlaying = new SimpleBooleanProperty(false);
        });
        // Listener to height UI size, to resize canvas dynamically
        view.pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            this.world.setBoardSize(this.world.getXLength(), ((newValue.intValue()) / (int) view.cellSize));
            this.view.resizeCanvas(this.world.getXLength(), this.world.getYLength());
            this.view.refresh(this.world.getPositions());
//            System.out.println("(LISTENER) STACKPANE HEIGHTPROPERTY - STACKP-X: " + view.pane.getWidth() + ", STACKP-Y: " + view.pane.getHeight() + " - CANVAS-X: " + view.canvas.getWidth() + ", CANVAS-Y: " + view.canvas.getHeight() + " - STACKP-Y: OLD " + oldValue + ", NEW " + newValue);
        });
        // Listener to width UI size, to resize canvas dynamically
        view.pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            this.world.setBoardSize(((newValue.intValue()) / (int) view.cellSize), this.world.getYLength());
            this.view.resizeCanvas(this.world.getXLength(), this.world.getYLength());
            this.view.refresh(this.world.getPositions());
//            System.out.println("(LISTENER) STACKPANE WIDTHPROPERTY - STACKP-X: " + view.pane.getWidth() + ", STACKP-Y: " + view.pane.getHeight() + " - CANVAS-X: " + view.canvas.getWidth() + ", CANVAS-Y: " + view.canvas.getHeight() + " - STACKP-X: OLD " + oldValue + ", NEW " + newValue);
        });
        // Toggle cells through mouse events
        view.canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getX() > 0 && event.getX() < view.canvas.widthProperty().intValue() && event.getY() > 0 && event.getY() < view.canvas.heightProperty().intValue()) {
                int x = (int) event.getX();
                int y = (int) event.getY();
//                System.out.println(x + ", " + y);
                int xc = x / (int) view.cellSize;
                int yc = y / (int) view.cellSize;
                this.world.toggleCell(xc, yc);
            }
        });
        view.canvas.setOnMouseDragged(event -> {
            if (event.getX() > 0 && event.getX() < view.canvas.widthProperty().intValue() && event.getY() > 0 && event.getY() < view.canvas.heightProperty().intValue()) {
                this.world.toggleCell((int) (event.getX() / (view.cellSize)), (int) (event.getY() / view.cellSize));
            }
        });
        // Torus on/off
        view.cTorus.selectedProperty().addListener((observable, oldValue, newValue) -> {
            view.torus = new SimpleBooleanProperty(newValue);
            this.world.setTorus(newValue);
        });
        // Slider to set the cell size
        view.cSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            view.cellSize = newValue.doubleValue();
            this.world.setBoardSize(((view.pane.widthProperty().getValue().intValue() - 5) / (int) view.cellSize), ((view.pane.heightProperty().getValue().intValue() - 5) / (int) view.cellSize));
        });
        // Set stroke to 0.07
        view.rbStroke.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                view.strokeGrid.set(0.07);
                view.refresh(this.world.getPositions());
            }
        });
        // Set stroke to 0.3
        view.rbStroke2.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                view.strokeGrid.set(0.3);
            view.refresh(this.world.getPositions());
        });
        // Set stroke to 1.5
        view.rbStroke3.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                view.strokeGrid.set(1.5);
            view.refresh(this.world.getPositions());
        });
        // Toggle button BooleanModel
        view.tb1.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (view.isPlaying.getValue()) {
                view.isPlaying.setValue(false);
                view.timeLine.pause();
                if (newValue) {
                    this.world.deleteObserver(this.view);
//                    System.out.println("-PRE-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    this.world = new BooleanModel(this.world.getXLength(), this.world.getYLength(), this.world.getPositions());
                    this.world.setTorus(view.torus.getValue().booleanValue());
                    this.world.addObserver(this.view);
//                    System.out.println("-BOOLEAN-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    view.refresh(this.world.getPositions());
//                    System.out.println("BooleanModel");
                }
                view.isPlaying.setValue(true);
                view.timeLine.play();
            } else {
                if (newValue) {
                    this.world.deleteObserver(this.view);
//                    System.out.println("-PRE-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    this.world = new BooleanModel(this.world.getXLength(), this.world.getYLength(), this.world.getPositions());
                    this.world.setTorus(view.torus.getValue().booleanValue());
                    this.world.addObserver(this.view);
//                    System.out.println("-BOOLEAN-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    view.refresh(this.world.getPositions());
//                    System.out.println("BooleanModel");
                }
            }
        });
        // Toggle button BitSetModel
        view.tb2.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (view.isPlaying.getValue()) {
                view.isPlaying.setValue(false);
                view.timeLine.pause();
                if (newValue) {
                    this.world.deleteObserver(this.view);
//                    System.out.println("-PRE-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    this.world = new BitSetModel(this.world.getXLength(), this.world.getYLength(), this.world.getPositions());
                    this.world.setTorus(view.torus.getValue().booleanValue());
                    this.world.addObserver(this.view);
//                    System.out.println("-BIT-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    view.refresh(this.world.getPositions());
//                    System.out.println("BitSetModel");
                }
                view.isPlaying.setValue(true);
                view.timeLine.play();
            } else {
                if (newValue) {
                    this.world.deleteObserver(this.view);
//                    System.out.println("-PRE-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    this.world = new BitSetModel(this.world.getXLength(), this.world.getYLength(), this.world.getPositions());
                    this.world.setTorus(view.torus.getValue().booleanValue());
                    this.world.addObserver(this.view);
//                    System.out.println("-BIT-SIZE " + this.world.getXLength() + ", " + this.world.getYLength());
                    view.refresh(this.world.getPositions());
//                    System.out.println("BitSetModel");
                }
            }
        });
    }

    /**
     * Invoked when a specific event of the type for which this handler is
     * registered happens.
     *
     * @param event the event which occurred
     */
    @Override
    public void handle(Event event) {
        // event from reset button
        if (event.getSource().equals(view.bReset)) {
            view.memory.clear();
            world.resetBoard();
            view.timeLine.stop();
            view.bRun.getStyleClass().remove("bPause");
            view.bRun.getStyleClass().add("bRun");
            view.bRun.setTooltip(new Tooltip("Run"));
            view.isPlaying = new SimpleBooleanProperty(false);
            view.currentGeneration.set(0);
            view.aliveCells.set(this.world.getPositions().size());
        }
        // event from fastForward button
        if (event.getSource().equals(view.bFastForward)) {
            if (view.tfGeneration.getText().isEmpty()) {
                view.generationJump = 1;
                this.world.developGeneration();
                view.currentGeneration.set(view.currentGeneration.intValue() + 1);
                view.aliveCells.set(this.world.getPositions().size());
            } else {
                view.generationJump = Integer.parseInt(view.tfGeneration.getText());
                for (int i = 0; i < view.generationJump; i++) {
                    this.world.developGeneration();
                    view.currentGeneration.set(view.currentGeneration.intValue() + 1);
                    view.aliveCells.set(this.world.getPositions().size());
                }
            }
        }
        // event from menuItem SaveAs
        if (event.getSource().equals(view.menuItemSaveAs)) {
            File saveFile = view.fileChooser.showSaveDialog(stage);
            if (saveFile != null) {
                view.outputPath = saveFile.getPath();
            }
            try {
                InputOutput.createFile(this.world, view.outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // event from menuItem Open
        if (event.getSource().equals(view.menuItemOpen)) {
            File openFile = view.fileChooser.showOpenDialog(stage);
            if (openFile != null) {
                view.inputPath = openFile.getPath();
            }
            Object[] tmp = InputOutput.readFile(view.inputPath);
            view.memory.clear();
            this.world = new BooleanModel((int) tmp[0], (int) tmp[1], (ArrayList<Integer[]>) tmp[2]);
            this.world.setTorus(view.torus.getValue());
            this.world.addObserver(this.view);
            view.timeLine.stop();
            view.bRun.getStyleClass().remove("bPause");
            view.bRun.getStyleClass().add("bRun");
            view.bRun.setTooltip(new Tooltip("Run"));
            view.isPlaying = new SimpleBooleanProperty(false);
            view.currentGeneration.setValue(0);
            view.aliveCells.setValue(this.world.getPositions().size());
            view.resizeStage();
            this.view.resizeCanvas(this.world.getXLength(), this.world.getYLength());
            this.view.refresh(this.world.getPositions());

        }
        // event from menuItem show grid
        if (event.getSource().equals(view.gridAdjust)) {
            if (view.hideGrid) {
                view.grid.set(true);
                view.gridAdjust.setText("Hide Grid");
                view.hideGrid = false;
            } else {
                view.grid.set(false);
                view.gridAdjust.setText("Show Grid");
                view.hideGrid = true;
            }
            view.isGridDisplayed = !view.isGridDisplayed;
            view.refresh(this.world.getPositions());
        }
    }
}
