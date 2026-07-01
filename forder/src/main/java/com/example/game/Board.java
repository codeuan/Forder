package com.example.game;

import javafx.scene.layout.GridPane;

public class Board {

    private final GridPane boardGrid;
    private final int boardSize;
    private final int cellSize;

    private final Cell[][] cells;

    private Cell selectedCell = null;

    private final Player player1;
    private final Player player2;

    public Board(GridPane boardGrid, int boardSize, int cellSize) {
        this.boardGrid = boardGrid;
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.cells = new Cell[boardSize][boardSize];

        // Player 1 starts with permission to move.
        this.player1 = new Player("Player 1", "red", true);

        // Player 2 waits.
        this.player2 = new Player("Player 2", "blue", false);
    }

    public void createBoard() {
        boardGrid.getChildren().clear();

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
        Player currentPlayer = getCurrentPlayer();

        // Safety check: if no player can move, stop.
        if (currentPlayer == null) {
            return;
        }

        // Optional rule: players can only claim empty cells.
        if (!cell.isEmpty()) {
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

        System.out.println("Player 1 score: " + getScore(player1));
        System.out.println("Player 2 score: " + getScore(player2));

        switchTurn();
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
}