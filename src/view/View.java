package view;

import controller.Controller;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Buildable;
import model.ModelEvent;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class View extends Application implements Observer {
    public Canvas canvas;
    public Scene scene;
    public Button bRun, bNext, bReset, bFastForward;
    public CheckBox cTorus;
    public Slider sSlider, cSlider;
    public VBox top;
    public HBox bottom;
    public StackPane pane;
    public TextField tfGeneration;
    public Label generationShow, liveCells;
    public MenuItem menuItemOpen, menuItemSaveAs, gridAdjust, party, menuItemClose, menuItemSizeBorder, trail;
    public FileChooser fileChooser;
    public double canvasWidth, canvasHeight, sceneWidth, sceneHeight, cellSize;
    public String inputPath, outputPath;
    public ToggleButton tb1, tb2;
    public BorderPane root;
    public boolean hideGrid;
    public boolean isGridDisplayed, partyOn , trailToggle;
    public ObjectProperty grid;
    public SimpleIntegerProperty currentGeneration, aliveCells;
    public SimpleDoubleProperty strokeGrid;
    public SimpleBooleanProperty isPlaying, torus;
    public int generationJump, r, g, b;
    public Timeline timeLine;
    public double speed = 100;
    public int cycleCount;
    public ToggleGroup tGroup;
    public RadioMenuItem rbStroke, rbStroke2, rbStroke3;
    public ArrayList<Integer[]> memory;
    Controller controller;
    Stage primaryStage;
    private GraphicsContext graphicsContext;

    /**
     * Constructor
     *
     * @param c instance of Controller
     */
    public View(Controller c) {
        this.controller = c;
        this.currentGeneration = new SimpleIntegerProperty(0);
        this.aliveCells = new SimpleIntegerProperty(0);
        this.isPlaying = new SimpleBooleanProperty(false);
        this.strokeGrid = new SimpleDoubleProperty(0.3);
        this.isGridDisplayed = true;
        this.memory = new ArrayList<>();
        this.partyOn = true;
        this.trailToggle = true;
        this.g = 254;
    }

    /**
     * Initialize view
     *
     * @param primaryStage stage
     * @throws Exception IOException
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        timeLine = new Timeline();

        // Init layout
        this.root = new BorderPane();
        this.scene = new Scene(root, sceneWidth, sceneHeight);
        root.setTop(getMenuAndToolbar());
        root.setLeft(null);
        root.setRight(null);
        root.setBottom(null);
        root.setCenter(getCenterPane());

        // Events triggered in menu
        menuItemOpen.setOnAction(controller);
        menuItemSaveAs.setOnAction(controller);
        menuItemClose.setOnAction(event -> Platform.exit());
        this.grid = new SimpleObjectProperty();
        this.gridAdjust.setOnAction(controller);
        //Events triggered in toolbar
        bFastForward.setOnAction(controller);
        bReset.setOnAction(controller);
        // Textfield just Integer
        tfGeneration.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                char ch = tfGeneration.getText().charAt(oldValue.intValue());
                if (!(ch >= '0' && ch <= '9')) {
                    tfGeneration.setText(tfGeneration.getText().substring(0, tfGeneration.getText().length() - 1));
                }
            }
        });
        // Set label currentGeneration
        currentGeneration.addListener((observable, oldValue, newValue) -> {
            generationShow.setText(currentGeneration.getValue().toString());
        });
        // Set label aliveCells
        aliveCells.addListener((observable, oldValue, newValue) -> {
            liveCells.setText(aliveCells.getValue().toString());
        });
        // Run button event
        bRun.setOnAction(event -> {
            if (isPlaying.getValue()) {
                timeLine.stop();
                this.cycleCount = Animation.INDEFINITE;
                timeLine.setCycleCount(this.cycleCount);
                this.bRun.getStyleClass().remove("bPause");
                this.bRun.getStyleClass().add("bRun");
                this.bRun.setTooltip(new Tooltip("Run"));
                this.isPlaying = new SimpleBooleanProperty(false);
            } else {
                timeLine.stop();
                this.cycleCount = Animation.INDEFINITE;
                timeLine.setCycleCount(this.cycleCount);
                this.bRun.getStyleClass().remove("bRun");
                this.bRun.getStyleClass().add("bPause");
                this.bRun.setTooltip(new Tooltip("Pause"));
                this.isPlaying = new SimpleBooleanProperty(true);
                timeLine.play();
            }
        });
        // Next button event
        bNext.setOnAction(event -> {
//            this.timeLine.pause();
            if (tfGeneration.getText().isEmpty()) {
                this.generationJump = 1;
            } else {
                this.generationJump = Integer.parseInt(tfGeneration.getText());
            }
            timeLine.stop();
            this.cycleCount = generationJump;
            this.timeLine.setCycleCount(this.cycleCount);
            this.bRun.getStyleClass().removeAll("button", "bRun", "bPause");
            this.bRun.getStyleClass().add("button");
            this.bRun.getStyleClass().add("bPause");
            this.bRun.setTooltip(new Tooltip("Pause"));
//            System.out.println(bRun.getStyleClass() + " : StyleClass after bNext method.");
            this.isPlaying = new SimpleBooleanProperty(true);
            this.timeLine.play();
        });

        // Set CSS
        scene.getStylesheets().add(getClass().getResource("viewGoL.css").toExternalForm());

        // Init stage
        this.primaryStage = primaryStage;
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(pane.getHeight() + 150);
        primaryStage.setMinWidth(735);
        primaryStage.setTitle("Game of Life");
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Build menubar and the toolbar
     *
     * @return VBox containing menu and toolbar
     */
    public VBox getMenuAndToolbar() {

        this.top = new VBox();
        this.fileChooser = new FileChooser();
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(txtFilter);

        // MenuBar with File and Graphics Settings
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        Menu menuView = new Menu("View");
        Menu menuSizeBorder = new Menu("Grid Stroke");

        this.tGroup = new ToggleGroup();
        this.rbStroke = new RadioMenuItem();
        rbStroke.setToggleGroup(tGroup);
        rbStroke.setText("Small");
        this.rbStroke2 = new RadioMenuItem();
        rbStroke2.setToggleGroup(tGroup);
        rbStroke2.setText("Standard");
        rbStroke2.setSelected(true);
        this.rbStroke3 = new RadioMenuItem();
        rbStroke3.setToggleGroup(tGroup);
        rbStroke3.setText("Big");
        menuSizeBorder.getItems().addAll(rbStroke, rbStroke2, rbStroke3);

        this.menuItemOpen = new MenuItem("Open...");
        this.menuItemSaveAs = new MenuItem("Save as...");
        this.menuItemClose = new MenuItem("Quit Game Of Life");
        this.menuItemSizeBorder = new MenuItem("Grid Stroke");
        this.gridAdjust = new MenuItem("Hide Grid");
        this.party = new MenuItem("Show Party");
        this.trail = new MenuItem("Show Trail");

        // Toolbar
        ToolBar toolBar = new ToolBar();
        bRun = new Button();
        bRun.getStyleClass().add("bRun");
        bRun.setTooltip(new Tooltip("Run"));
        bNext = new Button();
        bNext.setId("bNext");
        bNext.setTooltip(new Tooltip("Run to step"));
        bFastForward = new Button();
        bFastForward.setId("bFastForward");
        bFastForward.setTooltip(new Tooltip("Fast forward"));
        this.tfGeneration = new TextField();
        tfGeneration.setTooltip(new Tooltip("Set step size"));
        tfGeneration.setPromptText("1");
        tfGeneration.setId("tfGeneration");
        bReset = new Button();
        bReset.setTooltip(new Tooltip("Clear"));
        bReset.setId("bReset");
        cTorus = new CheckBox();
        cTorus.setText("Torus");
        cTorus.setSelected(false);

        cSlider = new Slider();
        cSlider.setTooltip(new Tooltip("Cellsize"));
        cSlider.setMaxWidth(100);
        cSlider.setMin(3);
        cSlider.setMax(10);
        cSlider.setValue(5);
        cSlider.setMinorTickCount(0);
        cSlider.setMajorTickUnit(1);
        cSlider.setBlockIncrement(1);
        cSlider.setShowTickMarks(false);
        cSlider.setSnapToTicks(true);

        sSlider = new Slider();
        sSlider.setTooltip(new Tooltip("Speed"));
        sSlider.setMaxWidth(100);
        sSlider.setMin(0.1);
        sSlider.setMax(6);
        sSlider.setValue(1.0);
        sSlider.setMinorTickCount(0);
        sSlider.setMajorTickUnit(0.1);
        sSlider.setBlockIncrement(0.1);
        sSlider.setShowTickMarks(false);
        sSlider.setSnapToTicks(true);

        VBox test = new VBox();
        VBox test1 = new VBox();
        VBox test2 = new VBox();
        test2.setSpacing(2);
        this.generationShow = new Label(currentGeneration.getValue().toString());
        generationShow.setTooltip(new Tooltip("Generation"));
        this.liveCells = new Label(aliveCells.getValue().toString());
        liveCells.setTooltip(new Tooltip("Live cells"));
        Label gen = new Label("Generation: ");
        Label cellAlive = new Label("Alive cells: ");
        Label timeLineRate = new Label("Alive cells: ");
        test1.getChildren().addAll(gen, cellAlive);
        test2.getChildren().addAll(sSlider, cSlider);
        test.getChildren().addAll(generationShow, liveCells);

        this.tb1 = new ToggleButton("Boolean");
        tb1.setSelected(true);
        this.tb2 = new ToggleButton("BitSet");
        ToggleGroup group = new ToggleGroup();
        tb1.setToggleGroup(group);
        tb2.setToggleGroup(group);

        sSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            timeLine.rateProperty().set((double) newValue);
            timeLineRate.setText(newValue.toString());
        });
        party.setOnAction(event -> {
            if (partyOn) {
                party.setText("Hide Party");
                this.partyOn = !this.partyOn;
            } else {
                party.setText("Show Party");
                this.partyOn = !this.partyOn;

            }
        });
        trail.setOnAction(event -> {
            if (trailToggle) {
                trail.setText("Hide Trail");
                this.trailToggle = !this.trailToggle;
            } else {
                trail.setText("Show Trail");
                this.trailToggle = !this.trailToggle;

            }
        });

        menuFile.getItems().addAll(menuItemOpen, menuItemSaveAs, new SeparatorMenuItem(), menuItemClose);
        menuView.getItems().addAll(menuSizeBorder, gridAdjust, party, trail);
        menuBar.getMenus().addAll(menuFile, menuView);
        toolBar.getItems().addAll(bRun, tfGeneration, bNext, bFastForward, bReset, new Separator(), cTorus, new Separator(), test2, new Separator(), tb1, tb2, new Separator(), test1, test);
        top.getChildren().addAll(menuBar, toolBar);

        return top;
    }

    /**
     * Get Stackpane includes Canvas
     *
     * @return stackPane with canvas
     */
    public StackPane getCenterPane() {
        this.pane = new StackPane();
        pane.setMinSize(0, 0);
        pane.setAlignment(Pos.TOP_LEFT);
        canvas = new Canvas(canvasWidth, canvasHeight);
        this.graphicsContext = canvas.getGraphicsContext2D();
        pane.getChildren().addAll(canvas);
        return pane;
    }

    /**
     * Resize stage
     */
    public void resizeStage() {
        this.primaryStage.setMinWidth(735);
        this.primaryStage.setWidth(sceneWidth);
        this.primaryStage.setHeight(sceneHeight + top.getHeight());

    }

    /**
     * Resize canvas proportional to cell size
     *
     * @param x new canvas width
     * @param y new canvas height
     */
    public void resizeCanvas(int x, int y) {
        this.canvas.setWidth(x * (int) this.cellSize);
        this.canvas.setHeight(y * (int) this.cellSize);
//        System.out.println("RESIZE CANVAS: " + this.canvas.getWidth() + ", " + this.canvas.getHeight());
    }

    /**
     * Paint grid and living cells
     *
     * @param positions living cells
     */
    public void refresh(ArrayList<Integer[]> positions) {
        this.canvasWidth = this.canvas.getWidth();
        this.canvasHeight = this.canvas.getHeight();

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
        if (!trailToggle && !this.memory.isEmpty()) {
            graphicsContext.setFill(Color.LIGHTGREEN);
            for (Integer[] cell : this.memory) {
                graphicsContext.fillRect(cell[0] * (int) cellSize, cell[1] * (int) cellSize, (int) cellSize, (int) cellSize);
            }
        }
        graphicsContext.setFill(Color.BLACK);
        if (!partyOn) {
            graphicsContext.setFill(Color.rgb(r, g, b));
            this.r = (int) (Math.random() * 253);
            this.g = (int) (Math.random() * 253);
            this.b = (int) (Math.random() * 253);
        }


        if (isGridDisplayed) {
            graphicsContext.setLineWidth(this.strokeGrid.get());

            for (int i = 0; i <= canvasWidth; i += (int) cellSize) {
                graphicsContext.strokeLine(i, 0, i, canvasHeight);
            }
            for (int i = 0; i <= canvasHeight; i += (int) cellSize) {
                graphicsContext.strokeLine(0, i, canvasWidth, i);
            }
        }
        for (Integer[] cell : positions) {
            graphicsContext.fillRect(cell[0] * (int) cellSize, cell[1] * (int) cellSize, (int) cellSize, (int) cellSize);
            if (!trailToggle && !this.memory.contains(cell)) {
                this.memory.add(cell);
            }
        }
    }

    /**
     * View update method
     *
     * @param o   Observable
     * @param arg Event
     */
    @Override
    public void update(final Observable o, final Object arg) {
        if (o instanceof Buildable) {
            final Buildable model = (Buildable) o;
            final ModelEvent event = (ModelEvent) arg;
            switch (event.getType()) {
                case RESET_BOARD:
                    refresh(model.getPositions());
                    break;
                case UPDATED_BOARD:
                    refresh(model.getPositions());
                    break;
                case CHANGE_SIZE:
                    resizeCanvas(model.getXLength(), model.getYLength());
                    refresh(model.getPositions());
                    break;
                default:
            }
        }
    }
}


