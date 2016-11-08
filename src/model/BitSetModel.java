package model;

import view.View;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Observable;

/**
 * @author Andreas Ellwanger, Christian Reiner, Lisa Stephan
 */
public class BitSetModel extends Observable implements Buildable {


    private BitSet board;
    private int xLength;
    private int yLength;
    private boolean torus;


    /**
     * Constructor (empty board)
     *
     * @param xLength width of board
     * @param yLength height of board
     */
    public BitSetModel(int xLength, int yLength) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.board = new BitSet(xLength * yLength);
    }

    /**
     * Constructor (board with living cells)
     *
     * @param xLength   width of board
     * @param yLength   height of board
     * @param positions coordinates of living cells (x,y)
     */
    public BitSetModel(int xLength, int yLength, ArrayList<Integer[]> positions) {
        this(xLength, yLength);
        initBoard(positions);
    }

    /**
     * Initialize board with living cells
     *
     * @param positions coordinates of living cells
     */
    @Override
    public void initBoard(ArrayList<Integer[]> positions) {
        for (Integer[] array : positions) {
            this.board.set(array[0] + array[1] * xLength);
        }
        System.out.println("--BitSet  INIT");
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
        BitSet newBoard = new BitSet(x * y);
        int j = 0;
        if (x > this.xLength) {
            for (int i = 0; i < this.xLength * this.yLength; j++) {
                if (j % x < this.xLength) {
                    newBoard.set(j, board.get(i));
                    i++;
                }
            }
        } else {
            for (int i = 0; i < x * y; j++) {
                if (j % this.xLength < x) {
                    newBoard.set(i, board.get(j));
                    i++;
                }
            }
        }
        this.board = newBoard;
        this.xLength = x;
        this.yLength = y;

        System.out.println("--BitSet  SET:" + getXLength() + "," + getYLength());
        setChanged();
        notifyObservers(new ModelEvent(EventTypes.CHANGE_SIZE));
    }

    /**
     * Clear board
     */
    @Override
    public void resetBoard() {
        this.board = new BitSet(xLength * yLength);
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
        for (int i = 0; i < xLength * yLength; i++) {
            if (board.get(i)) {
                positions.add(new Integer[]{i % xLength, i / xLength});
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
        return this.xLength;
    }

    /**
     * Get board height
     *
     * @return height of board (y length)
     */
    @Override
    public int getYLength() {
        return this.yLength;
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
        BitSet nextGenerationBoard = cloneGeneration(this.board);

        // Decide the fate of each cell
        for (int i = 0; i < xLength * yLength; i++) {
            final int numNeighbors = countNeighbors(this.board, i);
            // Cell dies, if under-/over-populated
            if ((numNeighbors < 2) || (numNeighbors > 3)) {
                nextGenerationBoard.set(i, false);
            }
            // No change
            if (numNeighbors == 2) {
                nextGenerationBoard.set(i, this.board.get(i));
            }
            // Cell stays alive, or new cell is born
            if (numNeighbors == 3) {
                nextGenerationBoard.set(i, true);
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
    private BitSet cloneGeneration(BitSet originalBoard) {
        BitSet clonedBoard = new BitSet(xLength * yLength);
        for (int i = 0; i < xLength * yLength; i++) {
            clonedBoard.set(i, originalBoard.get(i));
        }
        return clonedBoard;
    }

    /**
     * Count neighbors of one specific cell (x,y)
     *
     * @param board current board
     * @param i     specific cell
     * @return count of neighbors of cell(x,y)
     */
    private int countNeighbors(BitSet board, int i) {
        int numNeighbors = 0;
        if (!torus) {
            // Torus off
            // Look ABOVE, LEFT
            if (i - xLength - 1 >= 0 && (i - xLength - 1) % (xLength - 1) != 0) {
                numNeighbors = board.get(i - xLength - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE
            if (i - xLength >= 0) {
                numNeighbors = board.get(i - xLength) ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE, RIGHT
            if (i - xLength + 1 >= 0 && (i - xLength + 1) % xLength != 0) {
                numNeighbors = board.get(i - xLength + 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look LEFT
            if (i - 1 >= 0 && (i - 1) % (xLength - 1) != 0) {
                numNeighbors = board.get(i - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look RIGHT
            if ((i + 1) % xLength != 0) {
                numNeighbors = board.get(i + 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW, LEFT
            if ((i + xLength - 1) % (xLength - 1) != 0) {
                numNeighbors = board.get(i + xLength - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW
            numNeighbors = board.get(i + xLength) ? numNeighbors + 1 : numNeighbors;
            // Look BELOW, RIGHT
            if ((i + xLength + 1) % xLength != 0) {
                numNeighbors = board.get(i + xLength + 1) ? numNeighbors + 1 : numNeighbors;
            }
        } else {
            // Torus on
            // Look ABOVE, LEFT
            if (i - xLength - 1 >= 0 && (i - xLength - 1) % xLength != (xLength - 1)) {
                numNeighbors = board.get(i - xLength - 1) ? numNeighbors + 1 : numNeighbors;
            } else if (i - xLength < 0) {
                numNeighbors = board.get((i + xLength - 1) % xLength + (yLength - 1) * xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE
            if (i - xLength >= 0) {
                numNeighbors = board.get(i - xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i + (yLength - 1) * xLength) ? numNeighbors + 1 : numNeighbors;
            }
            // Look ABOVE, RIGHT
            if (i - xLength + 1 >= 0 && (i - xLength + 1) % xLength != 0) {
                numNeighbors = board.get(i - xLength + 1) ? numNeighbors + 1 : numNeighbors;
            } else if (i - xLength + 1 <= 0) {
                numNeighbors = board.get(((i + 1) % xLength) + (yLength - 1) * xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i - 2 * xLength + 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look LEFT
            if (i - 1 >= 0 && (i - 1) % xLength != (xLength - 1)) {
                numNeighbors = board.get(i - 1) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i + xLength - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look RIGHT
            if ((i + 1) % xLength != 0) {
                numNeighbors = board.get(i + 1) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i - xLength + 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW, LEFT
            if ((i + xLength - 1) % xLength != (xLength - 1) && i < (yLength - 1) * xLength) {
                numNeighbors = board.get(i + xLength - 1) ? numNeighbors + 1 : numNeighbors;
            } else if (i >= (yLength - 1) * xLength) {
                numNeighbors = board.get((i - 1) % xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i + 2 * xLength - 1) ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW
            if (i < (yLength - 1) * xLength) {
                numNeighbors = board.get(i + xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i % xLength) ? numNeighbors + 1 : numNeighbors;
            }
            // Look BELOW, RIGHT
            if ((i + xLength + 1) % xLength != 0 && i < (yLength - 1) * xLength) {
                numNeighbors = board.get(i + xLength + 1) ? numNeighbors + 1 : numNeighbors;
            } else if (i >= (yLength - 1) * xLength) {
                numNeighbors = board.get((i + 1) % xLength) ? numNeighbors + 1 : numNeighbors;
            } else {
                numNeighbors = board.get(i + 1) ? numNeighbors + 1 : numNeighbors;
            }
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
        this.board.flip(x + this.xLength * y);
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
