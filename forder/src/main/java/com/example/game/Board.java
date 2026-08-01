package com.example.game;

import java.util.HashSet;
import java.util.Set;

import com.example.game.Cell.Direction;

import javafx.scene.layout.GridPane;

public class Board {

    private final GridPane boardGrid;
    private final int boardSize;
    private final int cellSize;

    private final Cell[][] cells;

    private final Player player1;
    private final Player player2;

    private Player currentPlayer;

    private boolean gameOver = false;

    // Prevent the game returning to an earlier board position.
    private final Set<String> previousPositions = new HashSet<>();


    private static final Direction[][] DEFAULT_BOARD = {

        {
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.LEFT,
            Direction.LEFT
        },

        {
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.LEFT,
            Direction.LEFT
        },

        {
            Direction.LEFT,
            Direction.LEFT,
            Direction.RIGHT,
            Direction.RIGHT
        },

        {
            Direction.LEFT,
            Direction.LEFT,
            Direction.RIGHT,
            Direction.RIGHT
        }
    };


    public Board(GridPane boardGrid, int boardSize, int cellSize) {

        this.boardGrid = boardGrid;
        this.boardSize = boardSize;
        this.cellSize = cellSize;

        this.cells = new Cell[boardSize][boardSize];

        player1 = new Player(
            "Player 1",
            Direction.RIGHT
        );

        player2 = new Player(
            "Player 2",
            Direction.LEFT
        );

        currentPlayer = player1;
    }


    public void createBoard() {

        boardGrid.getChildren().clear();

        for (int row = 0; row < boardSize; row++) {

            for (int col = 0; col < boardSize; col++) {

                Cell cell = new Cell(
                    row,
                    col,
                    cellSize,
                    DEFAULT_BOARD[row][col]
                );

                cell.setOnMouseClicked(
                    event -> handleCellClick(cell)
                );

                cells[row][col] = cell;

                boardGrid.add(cell, col, row);
            }
        }

        previousPositions.clear();

        // Starting board counts as an already-seen position.
        previousPositions.add(encodeBoard());
    }


    private void handleCellClick(Cell cell) {

        if (gameOver) {
            return;
        }

        Direction previousDirection = cell.getDirection();

        // The entire move is simply:
        cell.flip();


        /*
         * LOOP RULE
         *
         * If this move produces a board that has existed before,
         * undo the move.
         */
        String newPosition = encodeBoard();

        if (previousPositions.contains(newPosition)) {

            cell.setDirection(previousDirection);

            System.out.println(
                "Illegal move: that would repeat an earlier position."
            );

            return;
        }

        previousPositions.add(newPosition);


        // Has somebody now won?
        Player winner = getWinner();

        if (winner != null) {

            gameOver = true;

            System.out.println(
                winner.getName()
                + " wins with "
                + winner.getDirection()
                + "!"
            );

            return;
        }


        switchTurn();
    }


    private Player getWinner() {

        if (PatternEngine.hasWinningLine(
                cells,
                player1.getDirection())) {

            return player1;
        }

        if (PatternEngine.hasWinningLine(
                cells,
                player2.getDirection())) {

            return player2;
        }

        return null;
    }


    private void switchTurn() {

        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }

        System.out.println(
            currentPlayer.getName() + "'s turn"
        );
    }


    private String encodeBoard() {

        StringBuilder state = new StringBuilder();

        for (int row = 0; row < boardSize; row++) {

            for (int col = 0; col < boardSize; col++) {

                if (cells[row][col].getDirection()
                        == Direction.RIGHT) {

                    state.append('R');

                } else {

                    state.append('L');
                }
            }
        }

        return state.toString();
    }


    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}