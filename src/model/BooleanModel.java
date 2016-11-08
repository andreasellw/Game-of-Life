package model;

import view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class BooleanModel extends Observable implements Buildable {

    private boolean[][] board;
    private boolean torus;

    /**
     * Constructor (empty board)
     *
     * @param xLength width of board
     * @param yLength height of board
     */
    public BooleanModel(int xLength, int yLength) {
        this.board = new boolean[yLength][xLength];
    }

    /**
     * Constructor (board with living cells)
     *
     * @param xLength   width of board
     * @param yLength   height of board
     * @param positions coordinates of living cells (x,y)
     */
    public BooleanModel(int xLength, int yLength, ArrayList<Integer[]> positions) {
        this(xLength, yLength);
        initBoard(positions);

    }

    /**
     * Initialize board with living cells
     *
     * @param positions coordinates of living cells (x,y)
     */
    @Override
    public void initBoard(ArrayList<Integer[]> positions) {
        for (Integer[] array : positions) {
            this.board[array[1]][array[0]] = true;
        }
        System.out.println("--Boolean INIT");
        setChanged();
        notifyObservers(new ModelEvent(EventTypes.CHANGE_SIZE));
    }

    /**
     * Resize board
     *
     * @param x new width of board
     * @param y new height of board
     */
    @Override
    public void setBoardSize(int x, int y) {
        if (board != null) {
            boolean[][] tmpOldBoard = board;
            this.board = new boolean[y][x];
            int minX = Math.min(tmpOldBoard[0].length, x);
            int minY = Math.min(tmpOldBoard.length, y);
            for (int tmpX = 0; tmpX < minX; tmpX++) {
                for (int tmpY = 0; tmpY < minY; tmpY++) {
                    if (tmpOldBoard[tmpY][tmpX]) {
                        this.board[tmpY][tmpX] = tmpOldBoard[tmpY][tmpX];
                    }
                }
            }
        } else {
            this.board = new boolean[getYLength()][getXLength()];
        }
        System.out.println("--Boolean SET:" + getXLength() + "," + getYLength());
        setChanged();
        notifyObservers(new ModelEvent(EventTypes.CHANGE_SIZE));
    }

    /**
     * Clear board
     */
    @Override
    public void resetBoard() {
        this.board = new boolean[getYLength()][getXLength()];
        setChanged();
        notifyObservers(new ModelEvent(EventTypes.RESET_BOARD));
    }

    /**
     * Get living cells
     *
     * @return coordinates of living cells
     */
    @Override
    public ArrayList<Integer[]> getPositions() {
        ArrayList<Integer[]> positions = new ArrayList<>();
        for (int y = 0; y < this.getYLength(); y++) {
            for (int x = 0; x < this.getXLength(); x++) {
                if (this.board[y][x]) {
                    positions.add(new Integer[]{x, y});
                }
            }
        }
        return positions;
    }

    /**
     * Get board width
     *
     * @return width of board (x length)
     */
    @Override
    public int getXLength() {
        return this.board[0].length;
    }

    /**
     * Get board height
     *
     * @return height of board (y length)
     */
    @Override
    public int getYLength() {
        return this.board.length;
    }

    /**
     * Set surface mode
     *
     * @param torus true = torus, false = hard limit
     */
    @Override
    public void setTorus(boolean torus) {
        this.torus = torus;
    }

    /**
     * Develop current generation to the next one
     */
    @Override
    public void developGeneration() {
        boolean[][] nextGenerationBoard = cloneGeneration(this.board);

        // Decide the fate of each cell
        for (int y = 0; y < getYLength(); ++y) {
            for (int x = 0; x < getXLength(); ++x) {
                final int numNeighbors = countNeighbors(this.board, y, x);
                // Cell dies, if under-/over-populated
                if ((numNeighbors < 2) || (numNeighbors > 3)) {
                    nextGenerationBoard[y][x] = false;
                }
                // No change
                if (numNeighbors == 2) {
                    nextGenerationBoard[y][x] = this.board[y][x];
                }
                // Cell stays alive, or new cell is born
                if (numNeighbors == 3) {
                    nextGenerationBoard[y][x] = true;
                }
            }
        }

        this.board = nextGenerationBoard;
        this.setChanged();
        notifyObservers(new ModelEvent(EventTypes.UPDATED_BOARD));
    }

    /**
     * Create copy of current board
     *
     * @param originalBoard current board
     * @return copy of originalBoard
     */
    private boolean[][] cloneGeneration(boolean[][] originalBoard) {
        boolean[][] clonedBoard = new boolean[originalBoard.length][];
        for (int y = 0; y < originalBoard.length; ++y) { // TODO ?
            clonedBoard[y] = Arrays.copyOf(originalBoard[y], originalBoard[y].length);
        }
        return clonedBoard;
    }

    /**
     * Count neighbors of one specific cell (x,y)
     *
     * @param board current board
     * @param x     x coordinate
     * @param y     y coordinate
     * @return count of neighbors of cell(x,y)
     */
    private int countNeighbors(boolean[][] board, int y, int x) {
        int numNeighbors = 0;
        // Torus off
        if (!torus) {
            // Look ABOVE, LEFT
            if ((y - 1 >= 0) && (x - 1 >= 0)) {
                numNeighbors = board[y - 1][x - 1] ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE
            if ((y - 1 >= 0) && (x < getXLength())) {
                numNeighbors = board[y - 1][x] ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE, RIGHT
            if ((y - 1 >= 0) && (x + 1 < getXLength())) {
                numNeighbors = board[y - 1][x + 1] ? numNeighbors + 1 : numNeighbors;
            }
            // Look LEFT
            if ((y >= 0) && (x - 1 >= 0)) {
                numNeighbors = board[y][x - 1] ? numNeighbors + 1 : numNeighbors;
            }
            // Look RIGHT
            if ((y < getYLength()) && (x + 1 < getXLength())) {
                numNeighbors = board[y][x + 1] ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW, LEFT
            if ((y + 1 < getYLength()) && (x - 1 >= 0)) {
                numNeighbors = board[y + 1][x - 1] ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW
            if ((y + 1 < getYLength()) && (x < getXLength())) {
                numNeighbors = board[y + 1][x] ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW, RIGHT
            if ((y + 1 < getYLength()) && (x + 1 < getXLength())) {
                numNeighbors = board[y + 1][x + 1] ? numNeighbors + 1 : numNeighbors;
            }
        } else {
            // Torus on
            int above, below;
            int left, right;
            above = y > 0 ? y - 1 : getYLength() - 1;
            below = y < getYLength() - 1 ? y + 1 : 0;
            left = x > 0 ? x - 1 : getXLength() - 1;
            right = x < getXLength() - 1 ? x + 1 : 0;
            // Look ABOVE, LEFT
            if (board[above][left])
                numNeighbors++;
            // Look ABOVE
            if (board[above][x])
                numNeighbors++;
            // Look ABOVE, RIGHT
            if (board[above][right])
                numNeighbors++;
            // Look LEFT
            if (board[y][left])
                numNeighbors++;
            // Look RIGHT
            if (board[y][right])
                numNeighbors++;
            // Look BELOW, LEFT
            if (board[below][left])
                numNeighbors++;
            // Look BELOW
            if (board[below][x])
                numNeighbors++;
            // Look BELOW, RIGHT
            if (board[below][right])
                numNeighbors++;
        }
        return numNeighbors;
    }

    /**
     * Toggle cell (living cell dies, dead cell is born)
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    @Override
    public void toggleCell(int x, int y) {
        this.board[y][x] = !this.board[y][x];
        setChanged();
        notifyObservers(new ModelEvent(EventTypes.UPDATED_BOARD));
    }

    /**
     * Add observer
     *
     * @param view instance of View
     */
    @Override
    public void addObserver(View view) {
        super.addObserver(view);
    }

    /**
     * Remove observer
     *
     * @param view instance of View
     */
    @Override
    public void deleteObserver(View view) {
        super.deleteObserver(view);
    }

}
