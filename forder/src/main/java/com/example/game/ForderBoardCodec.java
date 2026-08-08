package com.example.game;

/**
 * Adapter between your existing Cell[][] JavaFX board and the AI's 16-bit board.
 */
public final class ForderBoardCodec {

    private ForderBoardCodec() {
    }

    public static int encode(Cell[][] cells) {
        validateGrid(cells);

        int board = 0;

        for (int row = 0; row < ForderRules.ROWS; row++) {
            for (int col = 0; col < ForderRules.COLS; col++) {
                int bit = row * ForderRules.COLS + col;

                if (cells[row][col].getDirection() == Cell.Direction.RIGHT) {
                    board |= (1 << bit);
                }
            }
        }

        return board;
    }

    public static void applyMove(Cell[][] cells, int cell) {
        validateGrid(cells);

        int row = ForderRules.rowOf(cell);
        int col = ForderRules.colOf(cell);
        cells[row][col].flip();
    }

    private static void validateGrid(Cell[][] cells) {
        if (cells == null || cells.length != ForderRules.ROWS) {
            throw new IllegalArgumentException("Expected a 4x4 Cell array.");
        }

        for (Cell[] row : cells) {
            if (row == null || row.length != ForderRules.COLS) {
                throw new IllegalArgumentException("Expected a 4x4 Cell array.");
            }
        }
    }
}
