package com.example.game;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.SoundManager;
import com.example.game.Cell.CellState;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;


public class Board {

    private final GridPane boardGrid; //visual container.
    private final int boardSize;
    private final int cellSize;

    private final Cell[][] cells; //logical version of visual container.

    private Cell selectedCell = null;

    private final Player player1;
    private final Player player2;

    private final Random random = new Random();
    private Cell currentEitherCell;

    private final Label statusLabel;
    private final Button playAgainButton;

    private boolean gameOver = false;

    public void nextRound() {
        resolveCurrentEither();
        createNewEither();
    }

    private void resolveCurrentEither() {
        if (currentEitherCell == null) {
            return;
        }

        // If it is still purple, nobody claimed it.
        if (currentEitherCell.getState() == CellState.EITHER) {
            Player randomPlayer = random.nextBoolean() ? player1 : player2;

            currentEitherCell.setOwner(randomPlayer);
        }

        currentEitherCell = null;
    }


    private void createNewEither() {
        List<Cell> emptyCells = new ArrayList<>();  //store list of every empty cell.
    //List can only store Cell objects.
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = cells[row][col];

                if (cell.isEmpty()) {
                    emptyCells.add(cell);
                }
            }
        }

        // No empty squares left, so no new purple square can be created.
        if (emptyCells.isEmpty()) {
            return;
        }

        int randomIndex = random.nextInt(emptyCells.size());
        currentEitherCell = emptyCells.get(randomIndex);
        currentEitherCell.setState(CellState.EITHER); //choose a random empty cell and turn it into Either.
    }

    public Board(GridPane boardGrid, Label statusLabel, Button playAgainButton, int boardSize, int cellSize) {
        this.boardGrid = boardGrid;
        this.statusLabel = statusLabel;
        this.playAgainButton = playAgainButton;

        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.cells = new Cell[boardSize][boardSize];

        this.player1 = new Player("Player 1", "red", true);
        this.player2 = new Player("Player 2", "blue", false);

        this.playAgainButton.setOnAction(event -> resetGame());
    }

    public void createBoard() {
        boardGrid.getChildren().clear();
        SoundManager.playGameStart();
        selectedCell = null;
        currentEitherCell = null;
        gameOver = false;

        player1.setCanMove(true);
        player2.setCanMove(false);

        statusLabel.setText("Player 1's turn");
        playAgainButton.setVisible(false);
        playAgainButton.setManaged(false);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {

                Cell cell = new Cell(row, col, cellSize);

                cell.setOnMouseClicked(event -> handleCellClick(cell));

                cells[row][col] = cell;

                boardGrid.add(cell, col, row);
            }
        }
    }

    private void handleCellClick(Cell cell) {
        if (gameOver) {
            return;
        }

        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer == null) {
            return;
        }

        if (!cell.isEmpty() && !cell.isEither()) {
            System.out.println("This cell is already owned.");
            return;
        }

        System.out.println(
                currentPlayer.getName() +
                " clicked row " + cell.getRow() +
                ", column " + cell.getCol()
        );

        if (selectedCell != null) {
            selectedCell.deselect();
        }


        cell.setOwner(currentPlayer);
        cell.select();
        selectedCell = cell;

        nextRound();

        printScores();

        if (allCellsOwned()) {
            endGame();
            return;
        }

        switchTurn();
        updateTurnDisplay();
    }

    private Player getCurrentPlayer() {
        if (player1.canMove()) {
            return player1;
        }

        if (player2.canMove()) {
            return player2;
        }

        return null;
    }

    private void switchTurn() {
        player1.setCanMove(!player1.canMove());
        player2.setCanMove(!player2.canMove());
    }

    public int getScore(Player player) {
        int score = 0;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (cells[row][col].getOwner() == player) {
                    score++;
                }
            }
        }

        return score;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    private void printScores() {
    int player1Score = getScore(player1);
    int player2Score = getScore(player2);

    System.out.println("Player 1 score: " + player1Score);
    System.out.println("Player 2 score: " + player2Score);
    }

    private void updateTurnDisplay() {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer != null) {
            statusLabel.setText(
                    currentPlayer.getName() +
                    "'s turn\n" +
                    "Player 1: " + getScore(player1) +
                    " | Player 2: " + getScore(player2)
            );
        }
    }

    private boolean allCellsOwned() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                CellState state = cells[row][col].getState();

                if (state == CellState.EMPTY || state == CellState.EITHER) {
                    return false;
                }
            }
        }

        return true;
    }

    private void endGame() {
        gameOver = true;

        int player1Score = getScore(player1);
        int player2Score = getScore(player2);

        String result;

        if (player1Score > player2Score) {
            result = "Player 1 wins with " + player1Score + " points!\n" +
                    "Player 2 had " + player2Score + " points.";
        } else if (player2Score > player1Score) {
            result = "Player 2 wins with " + player2Score + " points!\n" +
                    "Player 1 had " + player1Score + " points.";
        } else {
            result = "It's a draw!\n" +
                    "Both players had " + player1Score + " points.";
        }

        System.out.println("GAME OVER");
        System.out.println(result);

        statusLabel.setText("GAME OVER " + result);

        playAgainButton.setVisible(true);
        playAgainButton.setManaged(true);
    }

    private void resetGame() {
        createBoard();
    }


}