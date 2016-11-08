package model;

import view.View;

import java.util.ArrayList;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public interface Buildable {

    /**
     * Initialize board with living cells
     *
     * @param positions coordinates of living cells
     */
    void initBoard(ArrayList<Integer[]> positions);

    /**
     * Resize board
     *
     * @param width  new width of board
     * @param length new height of board
     */
    void setBoardSize(int width, int length);

    /**
     * Clear board
     */
    void resetBoard();

    /**
     * Get living cells
     *
     * @return coordinates of living cells
     */
    ArrayList<Integer[]> getPositions();

    /**
     * Get board width
     *
     * @return width of board (x length)
     */
    int getXLength();

    /**
     * Get board height
     *
     * @return height of board (y length)
     */
    int getYLength();

    /**
     * Set surface mode
     *
     * @param torus true = torus, false = hard limit
     */
    void setTorus(boolean torus);

    /**
     * Develop current generation to the next one
     */
    void developGeneration();

    /**
     * Toggle cell (living cell dies, dead cell is born)
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    void toggleCell(int x, int y);

    /**
     * Add observer
     *
     * @param view instance of View
     */
    void addObserver(View view);

    /**
     * Remove observer
     *
     * @param view instance of View
     */
    void deleteObserver(View view);
}
