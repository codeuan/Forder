package com.example.game;

import com.example.game.Cell.Direction;

import javafx.scene.layout.GridPane;

public class Board {

    private static final Player HUMAN_PLAYER = Player.RED;
    private static final Player AI_PLAYER = Player.BLUE;

    private static final int AI_MAX_DEPTH = 8;
    private static final long AI_TIME_LIMIT_MS = 500;

    private final GridPane boardGrid;
    private final int boardSize;
    private final int cellSize;
    private final Cell[][] cells;

    private final ForderHistory history;
    private final ForderAI ai;

    private Player currentPlayer;
    private boolean gameOver;
    private Runnable onGameOver;

    public Board(GridPane boardGrid, int boardSize, int cellSize) {
        if (boardSize != ForderRules.ROWS || boardSize != ForderRules.COLS) {
            throw new IllegalArgumentException(
                    "Forder currently requires an "
                            + ForderRules.ROWS + "x" + ForderRules.COLS
                            + " board."
            );
        }

        this.boardGrid = boardGrid;
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.cells = new Cell[boardSize][boardSize];

        this.history = new ForderHistory();
        this.ai = new ForderAI(AI_PLAYER);

        this.currentPlayer = HUMAN_PLAYER;
        this.gameOver = false;
    }

    public void createBoard() {
        boardGrid.getChildren().clear();
        gameOver = false;
        currentPlayer = HUMAN_PLAYER;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Cell cell = new Cell(
                        row,
                        col,
                        cellSize,
                        startingDirection(row, col)
                );

                cell.setOnMouseClicked(event -> handleCellClick(cell));
                cells[row][col] = cell;
                boardGrid.add(cell, col, row);
            }
        }

        history.reset();

        long board = ForderBoardConverter.encode(cells);
        if (board != ForderRules.START_BOARD) {
            throw new IllegalStateException(
                    "GUI starting board does not match ForderRules.START_BOARD."
            );
        }

        System.out.println(HUMAN_PLAYER.getName() + "'s turn");
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    private Direction startingDirection(int row, int col) {
        int halfway = boardSize / 2;

        boolean top = row < halfway;
        boolean left = col < halfway;

        // Top-left and bottom-right quadrants are RIGHT/Red.
        return top == left ? Direction.RIGHT : Direction.LEFT;
    }

    private void handleCellClick(Cell cell) {
        if (gameOver || currentPlayer != HUMAN_PLAYER) {
            return;
        }

        int cellIndex = cell.getRow() * ForderRules.COLS + cell.getCol();
        boolean moveMade = makeMove(cellIndex);

        if (!moveMade || gameOver) {
            return;
        }

        currentPlayer = AI_PLAYER;
        makeAIMove();
    }

    /** Applies one real move to the logical board and JavaFX board. */
    private boolean makeMove(int cellIndex) {
        long currentBoard = ForderBoardConverter.encode(cells);

        if (!history.isLegalMove(currentBoard, cellIndex)) {
            System.out.println(
                    "Illegal move: that would repeat an earlier position."
            );
            return false;
        }

        long newBoard = history.playMove(currentBoard, cellIndex);
        ForderBoardConverter.applyMove(cells, cellIndex);

        long encodedBoard = ForderBoardConverter.encode(cells);
        if (encodedBoard != newBoard) {
            throw new IllegalStateException(
                    "GUI board and logical Forder board are out of sync."
            );
        }

        Player winner = ForderRules.winner(newBoard);
        if (winner != null) {
            System.out.println(
                    winner.getName()
                            + " wins with "
                            + winner.getDirection()
                            + "!"
            );
            finishGame();
        }

        return true;
    }

    private void makeAIMove() {
        if (gameOver) {
            return;
        }

        long board = ForderBoardConverter.encode(cells);
        System.out.println("AI is thinking...");

        ForderAI.SearchResult result = ai.chooseMove(
                board,
                history,
                AI_PLAYER,
                AI_MAX_DEPTH,
                AI_TIME_LIMIT_MS
        );

        if (result.cell() < 0) {
            System.out.println("No legal moves remain.");
            finishGame();
            return;
        }

        System.out.println(
                "AI chose cell "
                        + result.cell()
                        + " (row="
                        + result.row()
                        + ", col="
                        + result.col()
                        + ")"
        );

        System.out.println(
                "score="
                        + result.score()
                        + ", depth="
                        + result.completedDepth()
                        + ", nodes="
                        + result.nodesSearched()
        );

        boolean moveMade = makeMove(result.cell());
        if (!moveMade) {
            throw new IllegalStateException(
                    "AI returned an illegal move: " + result.cell()
            );
        }

        if (!gameOver) {
            currentPlayer = HUMAN_PLAYER;
            System.out.println(HUMAN_PLAYER.getName() + "'s turn");
        }
    }

    private void finishGame() {
        gameOver = true;

        if (onGameOver != null) {
            onGameOver.run();
        }
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}
