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

    public Board(
            GridPane boardGrid,
            int boardSize,
            int cellSize
    ) {
        this.boardGrid = boardGrid;
        this.boardSize = boardSize;
        this.cellSize = cellSize;

        cells = new Cell[boardSize][boardSize];

        history = new ForderHistory();
        ai = new ForderAI(AI_PLAYER);

        currentPlayer = HUMAN_PLAYER;
        gameOver = false;
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
                        DEFAULT_BOARD[row][col]
                );

                cell.setOnMouseClicked(
                        event -> handleCellClick(cell)
                );

                cells[row][col] = cell;

                boardGrid.add(cell, col, row);
            }
        }

        history.reset();

        /*
         * This is useful while developing:
         * make sure the graphical starting board and
         * ForderRules.START_BOARD really agree.
         */
        int encodedBoard =
                ForderBoardCodec.encode(cells);

        if (encodedBoard != ForderRules.START_BOARD) {
            throw new IllegalStateException(
                    "GUI starting board does not match "
                    + "ForderRules.START_BOARD."
            );
        }

        System.out.println(
                HUMAN_PLAYER.getName() + "'s turn"
        );
    }

    private void handleCellClick(Cell cell) {

        if (gameOver) {
            return;
        }

        /*
         * Don't allow the human to move for the AI.
         */
        if (currentPlayer != HUMAN_PLAYER) {
            return;
        }

        int cellIndex =
                cell.getRow() * ForderRules.COLS
                + cell.getCol();

        boolean moveMade = makeMove(cellIndex);

        if (!moveMade || gameOver) {
            return;
        }

        currentPlayer = AI_PLAYER;

        makeAIMove();
    }

    /**
     * Makes one real game move.
     *
     * Used by BOTH the human and the AI.
     */
    private boolean makeMove(int cellIndex) {

        int currentBoard =
                ForderBoardCodec.encode(cells);

        /*
         * Check the no-repeated-board rule.
         */
        if (!history.isLegalMove(
                currentBoard,
                cellIndex
        )) {

            System.out.println(
                    "Illegal move: that would repeat "
                    + "an earlier position."
            );

            return false;
        }

        /*
         * Update the logical game state.
         */
        int newBoard =
                history.playMove(
                        currentBoard,
                        cellIndex
                );

        /*
         * Update the JavaFX representation.
         */
        ForderBoardCodec.applyMove(
                cells,
                cellIndex
        );

        /*
         * Development sanity check.
         *
         * After changing the GUI, it should encode
         * to exactly the board that ForderHistory
         * calculated.
         */
        int encodedBoard =
                ForderBoardCodec.encode(cells);

        if (encodedBoard != newBoard) {
            throw new IllegalStateException(
                    "GUI board and logical board "
                    + "have become inconsistent."
            );
        }

        Player winner =
                ForderRules.winner(newBoard);

        if (winner != null) {

            gameOver = true;

            System.out.println(
                    winner.getName()
                    + " wins as "
                    + winner
                    + "!"
            );
        }

        return true;
    }

    private void makeAIMove() {

        if (gameOver) {
            return;
        }

        int board =
                ForderBoardCodec.encode(cells);

        System.out.println("AI is thinking...");

        ForderAI.SearchResult result =
                ai.chooseMove(
                        board,
                        history,
                        AI_PLAYER,
                        AI_MAX_DEPTH,
                        AI_TIME_LIMIT_MS
                );

        /*
         * -1 means the AI couldn't find any
         * legal remaining move.
         */
        if (result.cell() < 0) {

            gameOver = true;

            System.out.println(
                    "No legal moves remain."
            );

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

        boolean moveMade =
                makeMove(result.cell());

        if (!moveMade) {
            throw new IllegalStateException(
                    "AI returned an illegal move: "
                    + result.cell()
            );
        }

        if (!gameOver) {

            currentPlayer = HUMAN_PLAYER;

            System.out.println(
                    HUMAN_PLAYER.getName()
                    + "'s turn"
            );
        }
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}