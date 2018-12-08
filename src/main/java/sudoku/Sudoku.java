package sudoku;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a square Sudoku instance.
 */
public final class Sudoku {

    private final int squareSize;
    private final int boardSize;
    private final int[][] board;
    private final boolean[][] boardFixedPositions;

    /**
     * Creates a Sudoku in which takes initialization parameters from {@code config}.
     *
     * @param config initial Sudoku configuration
     */
    public static Sudoku of(@NotNull Config config) {
        return new Sudoku(config.squareSize, config.fixedQuantity);
    }

    // TODO: Comment
    public static Sudoku of(@NotNull FileConfig fileConfig) {
        List<int[]> sudokuLines = fileConfig.readSudokuLines();

        int boardSize = sudokuLines.size();
        int[][] board = new int[boardSize][boardSize];
        boolean[][] boardFixedPositions = new boolean[boardSize][boardSize];

        int rowIndex = 0;
        for (int[] line : sudokuLines) {
            // Copy raw line into board
            System.arraycopy(line, 0, board[rowIndex], 0, boardSize);

            for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
                boardFixedPositions[rowIndex][columnIndex] = line[columnIndex] > 0;
            }

            rowIndex++;
        }

        return new Sudoku(board, boardFixedPositions);
    }

    private Sudoku(int squareSize, int fixedQuantity) {
        if (squareSize < 1) throw new IllegalArgumentException("squareSize cannot be < 1");
        if (fixedQuantity < 0) throw new IllegalArgumentException("fixedQuantity cannot be < 0");

        this.squareSize = squareSize;
        boardSize = squareSize * squareSize;
        board = new int[boardSize][boardSize];
        boardFixedPositions = new boolean[boardSize][boardSize];

        initializeBoard(fixedQuantity);
    }

    private Sudoku(int[][] board, boolean[][] boardFixedPositions) {
        this.boardSize = board.length;
        this.squareSize = (int) Math.sqrt(boardSize);
        this.board = board;
        this.boardFixedPositions = boardFixedPositions;
    }

    private void initializeBoard(int fixedQuantity) {
        fillBoard();
        markFixedPositions(fixedQuantity);
        stripNonFixedValues();
    }

    private void fillBoard() {
        for (int squareRowIndex = 0; squareRowIndex < squareSize; squareRowIndex++) {
            for (int squareColumnIndex = 0; squareColumnIndex < squareSize; squareColumnIndex++) {
                fillBoardSquare(squareRowIndex * squareSize, squareColumnIndex * squareSize);
            }
        }
    }

    private void fillBoardSquare(int squareRowIndex, int squareColumnIndex) {
        LinkedList<Integer> shuffledValues = new LinkedList<>();
        for (int i = 1; i <= boardSize; i++) shuffledValues.add(i);
        Collections.shuffle(shuffledValues);

        for (int x = squareRowIndex; x < squareRowIndex + squareSize; x++) {
            for (int y = squareColumnIndex; y < squareColumnIndex + squareSize; y++) {
                int value = shuffledValues.pop();

                board[x][y] = value;
            }
        }
    }

    private void markFixedPositions(int fixedQuantity) {
        int i = 0;
        while (i < fixedQuantity) {
            Random random = new Random();
            Pair<Integer, Integer> randomPosition = Pair.of(random.nextInt(boardSize), random.nextInt(boardSize));
            int x = randomPosition.getLeft();
            int y = randomPosition.getRight();

            if (boardFixedPositions[x][y]) continue;

            boardFixedPositions[x][y] = true;
            i++;
        }
    }

    private void stripNonFixedValues() {
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (!boardFixedPositions[x][y]) board[x][y] = 0;
            }
        }
    }

    // TODO: Document
    public void populateNonFixed() {
        for (int squareRowIndex = 0; squareRowIndex < squareSize; squareRowIndex++) {
            for (int squareColumnIndex = 0; squareColumnIndex < squareSize; squareColumnIndex++) {
                populateNonFixedInSquare(squareRowIndex * squareSize, squareColumnIndex * squareSize);
            }
        }
    }

    // TODO: Document
    private void populateNonFixedInSquare(int squareRowIndex, int squareColumnIndex) {
        LinkedList<Integer> shuffledValues = new LinkedList<>();
        for (int i = 1; i <= boardSize; i++) shuffledValues.add(i);
        Collections.shuffle(shuffledValues);

        for (int x = squareRowIndex; x < squareRowIndex + squareSize; x++) {
            for (int y = squareColumnIndex; y < squareColumnIndex + squareSize; y++) {
                if (boardFixedPositions[x][y]) shuffledValues.remove((Integer)board[x][y]);
            }
        }

        for (int x = squareRowIndex; x < squareRowIndex + squareSize; x++) {
            for (int y = squareColumnIndex; y < squareColumnIndex + squareSize; y++) {
                if (!boardFixedPositions[x][y]) board[x][y] = shuffledValues.pop();
            }
        }
    }

    /**
     * @return the size of a square of this instance. For example, in a 3x3 instance the result would be {@code 3}.
     */
    public int getSquareSize() {
        return squareSize;
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
                if (x != rowIndex || y != columnIndex) {
                    squareRepetitions += board[x][y] == target ? 1 : 0;
                }
            }
        }

        return squareRepetitions;
    }

    /**
     * @param strategy to generate new neighbor
     *
     * @return a new instance of {@link Sudoku} generated by applying {@code strategy}
     */
    public Sudoku neighbor(@NotNull NeighborStrategy strategy) {
        switch (Objects.requireNonNull(strategy, "Neighbor strategy must not be null")) {
            case RANDOM_SWAP_BOARD:
            case RANDOM_SWAP_SQUARE:
                return swapToNeighbor(strategy);
            case RANDOM_ADD_ONE:
                return addOneToNeighbor();
            default:
                throw new IllegalArgumentException("Invalid NeighborStrategy -> " + strategy);
        }
    }

    private Sudoku addOneToNeighbor() {
        Random random = new Random();

        int x, y;

        do {
            x = random.nextInt(boardSize);
            y = random.nextInt(boardSize);
        } while (boardFixedPositions[x][y]);

        // Copy board data
        int[][] neighborBoard = new int[boardSize][boardSize];
        boolean[][] fixedBoardPositionsCopy = new boolean[boardSize][boardSize];

        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(board[i], 0, neighborBoard[i], 0, boardSize);
        }

        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(boardFixedPositions[i], 0, fixedBoardPositionsCopy[i], 0, boardSize);
        }

        // Add one to value
        int newValue = (neighborBoard[x][y] + 1) % boardSize;
        neighborBoard[x][y] = newValue == 0 ? 1 : newValue;

        return new Sudoku(neighborBoard, fixedBoardPositionsCopy);
    }

    private Sudoku swapToNeighbor(NeighborStrategy swapStrategy) {
        Random random = new Random();

        int x1, y1, x2, y2;
        boolean validSwap;

        do {
            if (swapStrategy == NeighborStrategy.RANDOM_SWAP_BOARD) {
                x1 = random.nextInt(boardSize);
                y1 = random.nextInt(boardSize);
                x2 = random.nextInt(boardSize);
                y2 = random.nextInt(boardSize);
            }  else if (swapStrategy == NeighborStrategy.RANDOM_SWAP_SQUARE) {
                int squareInitialRow = random.nextInt(squareSize) * squareSize;
                int squareInitialColumn = random.nextInt(squareSize) * squareSize;

                x1 = random.nextInt(squareSize) + squareInitialRow;
                y1 = random.nextInt(squareSize) + squareInitialColumn;
                x2 = random.nextInt(squareSize) + squareInitialRow;
                y2 = random.nextInt(squareSize) + squareInitialColumn;
            } else {
                throw new IllegalArgumentException("Neighbor strategy must be a swap one");
            }

            validSwap = (x1 != x2 || y1 != y2) && !boardFixedPositions[x1][y1] && !boardFixedPositions[x2][y2];
        } while (!validSwap);

        // Copy board data
        int[][] neighboardBoard = new int[boardSize][boardSize];
        boolean[][] fixedBoardPositionsCopy = new boolean[boardSize][boardSize];

        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(board[i], 0, neighboardBoard[i], 0, boardSize);
        }

        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(boardFixedPositions[i], 0, fixedBoardPositionsCopy[i], 0, boardSize);
        }

        // Swap values
        int buffer = neighboardBoard[x1][y1];
        neighboardBoard[x1][y1] = neighboardBoard[x2][y2];
        neighboardBoard[x2][y2] = buffer;

        return new Sudoku(neighboardBoard, fixedBoardPositionsCopy);
    }

    // TODO: Write documentation
    public void dump(@NotNull BufferedWriter writer) throws IOException {
        StringBuilder dumpBuilder = new StringBuilder();

        for (int lineIndex = 0; lineIndex < boardSize; lineIndex++) {
            for (int i = 0; i < boardSize; i++) {
                dumpBuilder.append(board[lineIndex][i]);
                if (i < boardSize - 1) dumpBuilder.append(' ');
            }

            if (lineIndex < boardSize - 1) dumpBuilder.append("\n");
        }

        writer.append(dumpBuilder);
    }

    public final static class Config {
        public final int squareSize;
        public final int fixedQuantity;

        /**
         * Creates a {@link Config} which describes a {@link Sudoku}'s:
         *      * Square size determined by {@code squareSize}
         *      * Fixed amount of positions determined by {@code fixedQuantity}
         *
         * @param squareSize    an integer >= {@literal 1}
         * @param fixedQuantity an integer >= {@literal 0}
         */
        public Config(int squareSize, int fixedQuantity) {
            if (squareSize < 1) throw new IllegalArgumentException("squareSize cannot be < 1");
            if (fixedQuantity < 0) throw new IllegalArgumentException("fixedQuantity cannot be < 0");

            this.squareSize = squareSize;
            this.fixedQuantity = fixedQuantity;
        }
    }

    // TODO: Comment
    public abstract static class FileConfig {
        protected final Path filePath;

        private FileConfig(@NotNull Path filePath) {
            this.filePath = filePath;
        }

        @NotNull
        abstract List<int[]> readSudokuLines();
    }

    // TODO: Comment
    public final static class SingleDigitValueFileConfig extends FileConfig {

        public SingleDigitValueFileConfig(@NotNull Path filePath) {
            super(filePath);
        }

        @NotNull
        public List<int[]> readSudokuLines() {
            Stream<int[]> sudokuLinesStream;

            try {
                sudokuLinesStream = Files.readAllLines(filePath).stream()
                        .filter(s -> !s.trim().isEmpty())
                        .map(s -> {
                            int[] line = new int[s.length()];
                            for (int i = 0; i < s.length(); i++) line[i] = Character.getNumericValue(s.charAt(i));
                            return line;
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file " + filePath);
            }

            return sudokuLinesStream.collect(Collectors.toList());
        }
    }

    // TODO: Comment
    public final static class ValueSeparatorFileConfig extends FileConfig {
        private final String stringSeparator;

        public ValueSeparatorFileConfig(@NotNull Path filePath, char separator) {
            super(filePath);
            stringSeparator = String.valueOf(separator);
        }

        @NotNull
        public List<int[]> readSudokuLines() {
            Stream<int[]> sudokuLinesStream;

            try {
                sudokuLinesStream = Files.readAllLines(filePath).stream()
                        .filter(s -> !s.trim().isEmpty())
                        .map(s -> {
                            String[] stringValues = s.split(stringSeparator);
                            int[] line = new int[s.length()];
                            for (int i = 0; i < stringValues.length; i++) {
                                line[i] = Integer.valueOf(stringValues[i]);
                            }
                            return line;
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file " + filePath);
            }

            return sudokuLinesStream.collect(Collectors.toList());
        }
    }

    public enum NeighborStrategy {
        RANDOM_SWAP_BOARD, RANDOM_SWAP_SQUARE, RANDOM_ADD_ONE
    }

    /**
     * Outputs {@link Sudoku} instances to files:
     * TODO
     */
    public static void main(String[] ignored) {
        Path datasetsPath = Paths.get(System.getProperty("user.dir"), "datasets", "params_fixation");
        try {
            Files.createDirectories(datasetsPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /*
         * First slot corresponds to square size, second to fixed quantity and third to instance number
         */
        String instancePathFormat = "s_%d_%d_%d.txt";
        float[] fixedQuantityPercentages = new float[]{0.2f, 0.4f};

        for (int squareSize = 3; squareSize <= 11; squareSize++) {
            for (float fixedQuantityPercentage : fixedQuantityPercentages) {
                int fixedQuantity = (int) Math.ceil(Math.pow(squareSize, 4) * fixedQuantityPercentage);
                for (int sudokuNumber = 1; sudokuNumber <= 1000; sudokuNumber++) {
                    Sudoku instance = Sudoku.of(new Config(squareSize, fixedQuantity));
                    Path instancePath = datasetsPath.resolve(String.format(instancePathFormat, squareSize, fixedQuantity, sudokuNumber));

                    try (BufferedWriter instanceWriter = Files.newBufferedWriter(Files.createFile(instancePath))) {
                        instance.dump(instanceWriter);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }

    }

}