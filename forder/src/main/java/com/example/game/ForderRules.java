package com.example.game;
/
/**
 * Pure rules/bitboard helpers for Forder.
 *
 * Board encoding:
 *   RIGHT / Red = 1
 *   LEFT  / Blue = 0
 *
 * Cell numbering:
 *    0  1  2  3
 *    4  5  6  7
 *    8  9 10 11
 *   12 13 14 15
 */
public final class ForderRules {

    public static final int ROWS = 4;
    public static final int COLS = 4;
    public static final int CELLS = ROWS * COLS;
    public static final int BOARD_COUNT = 1 << CELLS;

    /**
     * Official starting board:
     *
     * R R L L
     * R R L L
     * L L R R
     * L L R R
     */
    public static final int START_BOARD = 0xCC33;

    private static final int[] ROW_MASKS = {
            0x000F,
            0x00F0,
            0x0F00,
            0xF000
    };

    private ForderRules() {
    }

    /** Returns the board obtained by flipping exactly one cell. */
    public static int flip(int board, int cell) {
        validateCell(cell);
        return board ^ (1 << cell);
    }

    /** True when the given player has a complete horizontal winning row. */
    public static boolean hasWinningRow(int board, Player player) {
        for (int rowMask : ROW_MASKS) {
            if (player == Player.RED) {
                // Four RIGHT/Red cells => all four bits are 1.
                if ((board & rowMask) == rowMask) {
                    return true;
                }
            } else {
                // Four LEFT/Blue cells => all four bits are 0.
                if ((board & rowMask) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the winner on this board, or null when the board is non-terminal.
     *
     * A simultaneous Red and Blue winning row should not be reachable if play
     * stops immediately on the first win. If one is supplied anyway, this
     * method throws so that the rule ambiguity is not hidden.
     */
    public static Player winner(int board) {
        boolean red = hasWinningRow(board, Player.RED);
        boolean blue = hasWinningRow(board, Player.BLUE);

        if (red && blue) {
            throw new IllegalStateException(
                    "Board contains simultaneous RED and BLUE winning rows. "
                            + "Define a tie rule if this is legal in Forder."
            );
        }

        if (red) {
            return Player.RED;
        }
        if (blue) {
            return Player.BLUE;
        }
        return null;
    }

    public static boolean isTerminal(int board) {
        return winner(board) != null;
    }

    public static int rowOf(int cell) {
        validateCell(cell);
        return cell / COLS;
    }

    public static int colOf(int cell) {
        validateCell(cell);
        return cell % COLS;
    }

    public static String boardToString(int board) {
        StringBuilder out = new StringBuilder();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cell = row * COLS + col;
                boolean right = (board & (1 << cell)) != 0;
                out.append(right ? 'R' : 'L');
                if (col < COLS - 1) {
                    out.append(' ');
                }
            }
            if (row < ROWS - 1) {
                out.append(System.lineSeparator());
            }
        }

        return out.toString();
    }

    private static void validateCell(int cell) {
        if (cell < 0 || cell >= CELLS) {
            throw new IllegalArgumentException("Cell must be in range 0..15: " + cell);
        }
    }
}
