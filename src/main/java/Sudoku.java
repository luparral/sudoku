import java.util.Random;

public class Sudoku {

    private int dim;
    private final int low = 1;
    private final int high = 9;
    private int[][] board;

    // Crear un soduku Aleatorio de tamaño dimension
    public Sudoku(int dimension) {
        this.dim = dimension;
        this.board = new int[dim][dim];

        for (int i = 0; i < dim; i++) {
            int[] line = new Random().ints(dim, low, high).toArray();
            board[i] = line;
        }
    }

    public void show(){
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                System.out.println("position (" + i + ", " + j + ") = " + board[i][j]);
            }
        }
    }

    public static void main(String[] args){
        int dim = 3;
        Sudoku sudoku = new Sudoku(dim);
        sudoku.show();

    }

}