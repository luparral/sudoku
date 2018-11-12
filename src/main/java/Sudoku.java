
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class Sudoku {

    private int dim;
    private final int low = 1;
    private final int high = 9;
    private int[][] board;
    private int[][] boardFixedPositions;
    private int fixedQuantity;

    // Crear un soduku Aleatorio de tamaño dimension x dimension y una cantidad fixedQuantity de celdas con elementos fijos.

    public Sudoku(int dimension, int fixedQuantity) {
        this.dim = dimension*dimension;
        this.board = new int[dim][dim];
        this.boardFixedPositions = new int[dim][dim];
        this.fixedQuantity = fixedQuantity;

        initBoards();
        fixBoardPositions();

    }

    private void show() {
        for (int row = 0; row < dim; row++) {
            for (int column = 0; column < dim; column++) {
                String fixedPos = "0";
                if(boardFixedPositions[row][column] == 1) {
                    fixedPos = "X"; // Si tiene una X es porque esa posición es fija.
                }
                System.out.print(board[row][column]+"("+fixedPos+")"+" ");
            }
            System.out.println();
        }
    }

    public boolean hasDuplicatesElementsInRow(){
        for (int i = 0; i < dim; i++) {
            int[] row = this.board[i];
            System.out.println("row " + i + " : " +  Arrays.toString(row));
            if(hasDuplicates(row)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDuplicatesElementsInColumn(){
        for(int i=0; i<dim; i++){
            int[] column = new int[dim];
            for (int j=0; j<dim;j++) {
                column[j] = (board[j][i]);
            }
            System.out.println("column " + i + " : " +  Arrays.toString(column));
            if(hasDuplicates(column)) {
                return true;
            }
        }
        return false;
    }

    public static Object[] getColumn(Object[][] array, int index){
        Object[] column = new Object[array[0].length]; // Here I assume a rectangular 2D array!
        for(int i=0; i<column.length; i++){
            column[i] = array[i][index];
        }
        return column;
    }

//    public boolean hasRepetedElementsInQuadrant(){
//
//    }




    private void initBoards(){
        for (int i = 0; i < dim; i++) {
            int[] line = new Random().ints(dim, low, high).toArray();
            int zeroesLine[] = new int[dim];
            Arrays.fill(zeroesLine, 0);

            this.board[i] = line;
            this.boardFixedPositions[i] = zeroesLine;
        }
    }

    private void fixBoardPositions(){
        List<Pair<Integer,Integer>> possiblePositions = new ArrayList<>();

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                possiblePositions.add(Pair.of(i,j));
            }
        }

        int seed = 42;
        Collections.shuffle(possiblePositions, new Random(seed));

        List<Pair<Integer, Integer>> fixedPositions = possiblePositions.subList(0, fixedQuantity);

        fixedPositions.forEach(position -> {
            this.boardFixedPositions[position.getLeft()][position.getRight()] = 1;
        });
    }


    public static void main(String[] args){
        int dim = 2;
        int fixedQuantity = 1;
        Sudoku sudoku = new Sudoku(dim, fixedQuantity);
        sudoku.show();
        boolean noDupsRows = sudoku.hasDuplicatesElementsInRow();
        System.out.println("duplicates in rows: " + noDupsRows);
        boolean noDupsColumns = sudoku.hasDuplicatesElementsInColumn();
        System.out.println("duplicates in columns: " + noDupsColumns);
    }

    private boolean hasDuplicates(int[] array) {
        boolean duplicates=false;
        for (int j = 0 ;j<array.length;j++) {
            for (int k=j+1;k<array.length;k++) {
                if (k!=j && array[k] == array[j]) {
                    duplicates=true;
                }
            }
        }
        return duplicates;
    }

}