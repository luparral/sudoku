
import java.util.*;

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
        this.dim = dimension;
        this.board = new int[dim][dim];
        this.boardFixedPositions = new int[dim][dim];
        this.fixedQuantity = fixedQuantity;

        initBoards();
        fixBoardPositions();

    }

    public void show(){
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                System.out.println("position (" + i + ", " + j + ") = " + board[i][j]);
                System.out.println("position fixed board (" + i + ", " + j + ") = " + boardFixedPositions[i][j]);
            }
        }
    }

//    public boolean hasRepetedElementsInRow(){
//
//    }
//
//    [i0j0][i0j1][i0j2][i0j3]
//    [i1j0][i1j1][i1j2][i1j3]
//    [i2j0][i2j1][i2j2][i2j3]
//    [i3j0][i3j1][i3j2][i3j3]
//
//    public boolean hasRepetedElementsInColumn(){
//
//    }
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
        int dim = 5;
        int fixedQuantity = 5;
        Sudoku sudoku = new Sudoku(dim, fixedQuantity);
        sudoku.show();


    }

}