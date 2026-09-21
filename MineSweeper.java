// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2024T2, Assignment 3
 * Name: Ryan   Treleaven   
 * Username: Trelearyan 
 * ID:  300659016
 */

import ecs100.*;
import java.awt.Color;
import javax.swing.JButton;

/**
 *  Simple 'Minesweeper' program.
 *  There is a grid of squares, some of which contain a mine.
 *  
 *  The user can click on a square to either expose it or to
 *  mark/unmark it.
 *  
 *  If the user exposes a square with a mine, they lose.
 *  Otherwise, it is uncovered, and shows a number which represents the
 *  number of mines in the eight squares surrounding that one.
 *  If there are no mines adjacent to it, then all the unexposed squares
 *  immediately adjacent to it are exposed (and so on)
 *
 *  If the user marks a square, then they cannot expose the square,
 *  (unless they unmark it first)
 *  When all the squares without mines are exposed, the user has won.
 */
public class MineSweeper {

    public static final int ROWS = 15;
    public static final int COLS = 15;

    public static final double LEFT = 10; 
    public static final double TOP = 10;
    public static final double SQUARE_SIZE = 20;

    // Fields
    private boolean marking;

    private Square[][] squares;

    private JButton mrkButton;
    private JButton expButton;
    Color defaultColor;

    /** Set up the GUI: buttons and mouse to play the game */
    public void setupGUI(){
        UI.setMouseListener(this::doMouse);
        UI.addButton("New Game", this::makeGrid);
        this.expButton = UI.addButton("Expose", ()->setMarking(false));
        this.mrkButton = UI.addButton("Mark", ()->setMarking(true));
        UI.addButton("AI Helper", this::AI);
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.0);
    }

    /** Respond to mouse events */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")){
            int row = (int)((y-TOP)/SQUARE_SIZE);
            int col = (int)((x-LEFT)/SQUARE_SIZE);
            if (row>=0 && row < ROWS && col >= 0 && col < COLS){
                if (marking) { mark(row, col);}
                else         { tryExpose(row, col); }
            }
        }
    }

    // Other Methods
   /**
     * Mark (or unmark) the square.
     * If the square is exposed, don't do anything,
     * If it is marked, unmark it and redraw,
     * otherwise mark it and redraw.
     */
    public void mark(int row, int col){
        /*# YOUR CODE HERE */
        Square square = squares[row][col];
        if (square.isExposed()){        //if the square is exposed, don't do anything
            return;
        }
        else if (square.isMarked()){    //if square is marked, unmark it and redraw
            square.unMark();
        }
        else{
            square.mark();              //otherwise mark it and redraw
        }
        square.draw(row, col);
    }


    /** 
     * Respond to the player clicking on a square to expose it
     * - if it is already exposed or marked, do nothing.
     * - if it's a mine: lose (call drawLose()) 
     * - otherwise expose it (call exposeSquareAt)
     * then check to see if the player has won and call drawWon() if they have.
     * (This method is not recursive)
     */
    public void tryExpose(int row, int col){
        /*# YOUR CODE HERE */
        if (squares[row][col].isExposed()||squares[row][col].isMarked()){ //if it is already exposed or marked, do nothing
            return;
        }
        else if (squares[row][col].hasMine()){  //if it's a mine: lose (call drawLose()) 
            drawLose();
            return;
        }
        else{   //otherwise expose it 
            exposeSquareAt(row, col);
        }
        if (hasWon()){ //check to see if the player has won
            drawWin();
        }
    }

    /** 
     *  Ensures that the square at row and col is exposed.
     *  If it is already exposed, do nothing.
     *  Otherwise,
     *    Expose it and redraw it.
     *    If the number of adjacent mines of this square is 0, then none of
     *      its neighbours have mines, so
     *      expose all its eight neighbours 
     *      (and if they have no adjacent mines, expose their neighbours, and ....)
     *      (be careful not to go over the edges of the map)
     */
    public void exposeSquareAt(int row, int col){
        /*# YOUR CODE HERE */
        if (!squares[row][col].isExposed()){
            squares[row][col].setExposed(); // exposes the square
            squares[row][col].draw(row, col);
            if (squares[row][col].getAdjacentMines() == 0){
                if(row < ROWS - 1){exposeSquareAt(row+1, col);} // bottom
                if(row > 0){exposeSquareAt(row-1, col);}        // above
                if(col < COLS - 1){exposeSquareAt(row, col+1);} // right
                if(col > 0){exposeSquareAt(row, col-1);}        // left
                if(row != 0 && col !=0){exposeSquareAt(row-1, col-1);}                 // top left
                if(row != ROWS - 1 && col != 0){exposeSquareAt(row+1, col-1);}         // bottom left
                if(row != 0 && col != COLS - 1){exposeSquareAt(row-1, col+1);}         // top right
                if(row != ROWS - 1 && col != COLS - 1) {exposeSquareAt(row+1, col+1);} // bottom right
            }
        }
    }

    /** 
     * Returns true if and only if the player has won:
     * If any square without a mine is not exposed, then the player has not won yet.
     * If all the squares without a mine have been exposed, then the player has won.
     * (It doesn't matter if the squares with a mine have been marked or not).
     */
    public boolean hasWon(){
        /*# YOUR CODE HERE */
        for (int i=0; i<ROWS; i++){     //goes through each square 
            for (int j=0; j<COLS; j++){
                if (!squares[i][j].hasMine() && !squares[i][j].isExposed()){
                    return false;       // checks if any square without a mine is not exposed; returns false player has not won yet
                }
            }
        }
        return true;
    }
    
    /*
     * a do while loop that will continue to solve the game has long it is 
     * possible. the helper iterates through each square and attempts to mark bombs 
     * or expose safe squares based on the what the user sees.
     */
    
    public void AI() {
        boolean AIsolving;
        do {
            AIsolving = false;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    Square square = squares[row][col];
                    if (square.isExposed() && square.getAdjacentMines() > 0) {
                        int adjacentMines = square.getAdjacentMines();
                        int AdjMarked = countAdjMarkedSquares(row, col);
                        int AdjHidden = countAdjHiddenSquares(row, col);
    
                        // if number of remaining squares is same as adjacent mines, mark as bombs
                        if (AdjHidden > 0 && adjacentMines == AdjHidden + AdjMarked) {
                            markAdjBombs(row, col);
                            AIsolving = true;
                        }
                        
                        // if number of adjacent mines is same as marked, expose the remaining adjacent squares
                        if (AdjHidden > 0 && adjacentMines == AdjMarked) {
                            exposeAdjSquares(row, col);
                            AIsolving = true;
                        }
                    }
                }
            }
        } while (AIsolving);
        if(hasWon()){ 
            drawWin();
        }
    }
    
    /*
     * goes through each adjacent square and counts the number of squares that
     * have been marked by the user (assumes that the marked squares are correctly
     * marked, otherwise AI Helper fails)
     */
    
    public int countAdjMarkedSquares(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && squares[r][c].isMarked()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /*
     * goes through each adjacent square and counts the number of squares that
     * haven;t been exposed yet
     */
    
    public int countAdjHiddenSquares(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && !squares[r][c].isExposed() && !squares[r][c].isMarked()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /*
     * marks the adjacent bomb
     */
    
    public void markAdjBombs(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && !squares[r][c].isExposed() && !squares[r][c].isMarked()) {
                    squares[r][c].mark();
                    squares[r][c].draw(r, c);
                }
            }
        }
    }
    
    /*
     * exposes the adjacent square
     */
    
    public void exposeAdjSquares(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && !squares[r][c].isExposed() && !squares[r][c].isMarked()) {
                    exposeSquareAt(r, c);
                }
            }
        }
    }
    // completed methods

 
    /**
     * Respond to the Mark and Expose buttons:
     * Remember whether the user is currently "Marking" or "Exposing"
     * Change the colour of the "Mark", "Expose" buttons
     */
    public void setMarking(boolean v){
        marking=v;
        if (marking) {
            mrkButton.setBackground(Color.red);
            expButton.setBackground(null);
        }
        else {
            expButton.setBackground(Color.red);
            mrkButton.setBackground(null);
        }
    }

    /**
     * Construct and draw a grid with random mines.
     * Compute the number of adjacent mines in each Square
     */
    public void makeGrid(){
        UI.clearGraphics();
        this.squares = new Square[ROWS][COLS];
        for (int row=0; row < ROWS; row++){
            for (int col=0; col<COLS; col++){
                boolean isMine = Math.random()<0.1;     // approx 1 in 10 squares is a mine 
                this.squares[row][col] = new Square(isMine);
                this.squares[row][col].draw(row, col);
            }
        }
        // now compute the number of adjacent mines for each square
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                int count = 0;
                //look at each square in the neighbourhood.
                for (int r=Math.max(row-1,0); r<Math.min(row+2, ROWS); r++){
                    for (int c=Math.max(col-1,0); c<Math.min(col+2, COLS); c++){
                        if (squares[r][c].hasMine())
                            count++;
                    }
                }
                if (this.squares[row][col].hasMine())
                    count--;  // we weren't suppose to count this square, just the adjacent ones.

                this.squares[row][col].setAdjacentMines(count);
            }
        }
    }

    /** Draw a message telling the player they have won */
    public void drawWin(){
        UI.setFontSize(28);
        UI.drawString("You Win!", LEFT + COLS*SQUARE_SIZE + 20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Draw a message telling the player they have lost
     * and expose all the squares and redraw them
     */
    public void drawLose(){
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                squares[row][col].setExposed();
                squares[row][col].draw(row, col);
            }
        }
        UI.setFontSize(28);
        UI.drawString("You Lose!", LEFT + COLS*SQUARE_SIZE+20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Return a grid of integers, showing the visible state of the board:
     * -1 for any square that is not exposed
     * 0 - 8 for any exposed square, saying how many mines are adjacent to it.
     */
    public int[][] getVisibleState(){
        int[][] ans = new int[ROWS][COLS];
        for (int r=0; r<ROWS ; r++){
            for (int c=0; c<COLS; c++){
                ans[r][c] = squares[r][c].isExposed()?(squares[r][c].getAdjacentMines()):-1;
            }
        }
        return ans;
    }

    /** 
     * Construct a new MineSweeper object
     * and set up the GUI
     */
    public static void main(String[] arguments){
        MineSweeper ms = new MineSweeper();
        ms.setupGUI();
        ms.setMarking(false);
        ms.makeGrid();
    }

}
