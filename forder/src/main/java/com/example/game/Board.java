package com.example.game;

import com.example.game.Cell.Direction;

import javafx.scene.layout.GridPane;

public class Board {

    private final GridPane boardGrid;
    private final int boardSize;
    private final int cellSize;

    private final Cell[][] cells;

    // --- CHANGED: The GUI now uses the same Player values as the AI core. ---
    private static final Player HUMAN_PLAYER = Player.RED;
    private static final Player AI_PLAYER = Player.BLUE;

    // --- CHANGED: Board now owns the real history and AI used during a game. ---
    private final ForderHistory history;
    private final ForderAI ai;

    private Player currentPlayer;
    private boolean gameOver;

    // --- CHANGED: AI settings live in one obvious place. ---
    private static final int AI_MAX_DEPTH = 8;
    private static final long AI_TIME_LIMIT_MS = 500;

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

        // --- CHANGED: The AI/rules codec is specifically a 4x4 implementation. ---
        if (boardSize != ForderRules.ROWS) {
            throw new IllegalArgumentException(
                    "Forder currently requires a 4x4 board."
            );
        }

        this.boardGrid = boardGrid;
        this.boardSize = boardSize;
        this.cellSize = cellSize;

        this.cells = new Cell[boardSize][boardSize];

        // --- CHANGED: Replaces the old Player objects and String/HashSet history. ---
        this.history = new ForderHistory();
        this.ai = new ForderAI(AI_PLAYER);

        this.currentPlayer = HUMAN_PLAYER;
        this.gameOver = false;
    }

    public void createBoard() {

        boardGrid.getChildren().clear();

        // --- CHANGED: createBoard() now fully resets a game. ---
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

        // --- CHANGED: ForderHistory now owns repetition checking. ---
        history.reset();

        // --- CHANGED: Sanity-check that the JavaFX board and bitboard agree. ---
        int board = ForderBoardConverter.encode(cells);

        if (board != ForderRules.START_BOARD) {
            throw new IllegalStateException(
                    "GUI starting board does not match ForderRules.START_BOARD."
            );
        }

        System.out.println(HUMAN_PLAYER.getName() + "'s turn");
    }

    private void handleCellClick(Cell cell) {

        if (gameOver) {
            return;
        }

        // --- CHANGED: Ignore clicks while it is the AI's turn. ---
        if (currentPlayer != HUMAN_PLAYER) {
            return;
        }

        // --- CHANGED: Convert the clicked row/column into the AI's 0..15 cell index. ---
        int cellIndex =
                cell.getRow() * ForderRules.COLS
                + cell.getCol();

        boolean moveMade = makeMove(cellIndex);

        if (!moveMade || gameOver) {
            return;
        }

        // --- CHANGED: After a successful human move, hand the turn to the AI. ---
        currentPlayer = AI_PLAYER;
        makeAIMove();
    }

    /**
     * Applies one real move to both representations of the game:
     * the integer bitboard/history and the JavaFX Cell[][] board.
     *
     * This is deliberately used for BOTH human and AI moves so the two paths
     * cannot quietly develop different rules.
     */
    private boolean makeMove(int cellIndex) {

        int currentBoard = ForderBoardConverter.encode(cells);

        // --- CHANGED: One shared repetition rule for human and AI moves. ---
        if (!history.isLegalMove(currentBoard, cellIndex)) {
            System.out.println(
                    "Illegal move: that would repeat an earlier position."
            );
            return false;
        }

        // Record the logical move first.
        int newBoard = history.playMove(
                currentBoard,
                cellIndex
        );

        // --- CHANGED: ForderBoardConverter is now the bridge back to the JavaFX board. ---
        ForderBoardConverter.applyMove(
                cells,
                cellIndex
        );

        // --- CHANGED: Catch any accidental desynchronisation immediately. ---
        int encodedBoard = ForderBoardConverter.encode(cells);

        if (encodedBoard != newBoard) {
            throw new IllegalStateException(
                    "GUI board and logical Forder board are out of sync."
            );
        }

        // --- CHANGED: Winner comes from ForderRules, which now checks rows + columns. ---
        Player winner = ForderRules.winner(newBoard);

        if (winner != null) {
            gameOver = true;

            System.out.println(
                    winner.getName()
                    + " wins with "
                    + winner.getDirection()
                    + "!"
            );

            return true;
        }

        return true;
    }

    // --- CHANGED: This is the actual connection between Board and ForderAI. ---
    private void makeAIMove() {

        if (gameOver) {
            return;
        }

        int board = ForderBoardConverter.encode(cells);

        System.out.println("AI is thinking...");

        ForderAI.SearchResult result = ai.chooseMove(
                board,
                history,
                AI_PLAYER,
                AI_MAX_DEPTH,
                AI_TIME_LIMIT_MS
        );

        if (result.cell() < 0) {
            gameOver = true;
            System.out.println("No legal moves remain.");
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

            System.out.println(
                    HUMAN_PLAYER.getName() + "'s turn"
            );
        }
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}
