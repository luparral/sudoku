import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a square Sudoku instance.
 */
public final class Sudoku {

    private static final int MINIMUM = 1;
    private static final int MAXIMUM = 9;

    private final int squareSize;
    private final int boardSize;
    private final int[][] board;
    private final boolean[][] boardFixedPositions;
    private final int fixedQuantity;

    /**
     * Creates a Sudoku in which its squares have a size of {@code squareSize} and the board a size of
     * {@code squareSize * squareSize}.
     * The amount of fixed positions is determined by {@code fixedQuantity}.
     *
     * @param squareSize an integer >= {@literal 1}
     * @param fixedQuantity an integer >= {@literal 0}
     */
    public Sudoku(int squareSize, int fixedQuantity) {
        if (squareSize < 1) throw new IllegalArgumentException("squareSize cannot be < 1");
        if (fixedQuantity < 0) throw new IllegalArgumentException("fixedQuantity cannot be < 0");

        this.squareSize = squareSize;
        boardSize = squareSize * squareSize;
        board = new int[boardSize][boardSize];
        boardFixedPositions = new boolean[boardSize][boardSize];
        this.fixedQuantity = fixedQuantity;

        fillBoard();
        fillFixedPositions();
    }

    private void fillBoard() {
        for (int i = 0; i < boardSize; i++) {
            this.board[i] = new Random().ints(boardSize, MINIMUM, MAXIMUM + 1).toArray();
        }
    }

    private void fillFixedPositions() {
        List<Pair<Integer, Integer>> possiblePositions = new ArrayList<>();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                possiblePositions.add(Pair.of(i, j));
            }
        }

        int seed = 42;
        Collections.shuffle(possiblePositions, new Random(seed));

        List<Pair<Integer, Integer>> fixedPositions = possiblePositions.subList(0, fixedQuantity);

        fixedPositions.forEach(position -> boardFixedPositions[position.getLeft()][position.getRight()] = true);
    }

    /**
     * @return the amount of repetitions present in this {@link Sudoku}
     */
    public int repetitions() {
        int repetitions = 0;

        for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
            for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
                int current = board[rowIndex][columnIndex];

                repetitions += countRowRepetitions(current, rowIndex, columnIndex);
                repetitions += countColumnRepetitions(current, rowIndex, columnIndex);
                repetitions += countSquareRepetitions(current, rowIndex, columnIndex);
            }
        }

        return repetitions;
    }

    private int countRowRepetitions(int target, int rowIndex, int columnIndex) {
        int rowRepetitions = 0;

        for (int i = columnIndex - 1; i >= 0; i--) rowRepetitions += board[rowIndex][i] == target ? 1 : 0;
        for (int i = columnIndex + 1; i < boardSize; i++) rowRepetitions += board[rowIndex][i] == target ? 1 : 0;

        return rowRepetitions;
    }

    private int countColumnRepetitions(int target, int rowIndex, int columnIndex) {
        int columnRepetitions = 0;

        for (int i = rowIndex - 1; i >= 0; i--) columnRepetitions += board[i][columnIndex] == target ? 1 : 0;
        for (int i = rowIndex + 1; i < boardSize; i++) columnRepetitions += board[i][columnIndex] == target ? 1 : 0;

        return columnRepetitions;
    }

    private int countSquareRepetitions(int target, int rowIndex, int columnIndex) {
        int squareRepetitions = 0;

        /*
         * Calculate the index of the square in the board.
         * For example, in a 3x3 Sudoku, the position (3, 5) is in the Square (1, 1). Since that's indexed, that Square
         * is in the middle of the board.
         */
        int squareRowIndex = rowIndex / squareSize;
        int squareColumnIndex = columnIndex / squareSize;

        /*
         * Calculate the starting index of the square relative to the board.
         * For example, in a 3x3 Sudoku, the position (3, 5) is in the Square (1, 1). The starting position of that
         * Square corresponds to the position (3, 3) and ends at (5, 5).
         */
        int boardRowIndex = squareRowIndex * squareSize;
        int boardColumnIndex = squareColumnIndex * squareSize;

        for (int x = boardRowIndex; x < boardRowIndex + squareSize; x++) {
            for (int y = boardColumnIndex; y < boardColumnIndex + squareSize; y++) {
                squareRepetitions += board[x][y] == target ? 1 : 0;
            }
        }

        return squareRepetitions;
    }

    /**
     * Prints this {@link Sudoku}'s state to {@link System#out}.
     */
    public void show() {
        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                String positionRepresentation = boardFixedPositions[row][column] ? "X" : "0";

                System.out.print(board[row][column] + "(" + positionRepresentation + ")" + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int dim = 3;
        int fixedQuantity = 9;
        Sudoku sudoku = new Sudoku(dim, fixedQuantity);
        sudoku.show();
        System.out.println("#Duplicates = " + sudoku.repetitions());
    }

}