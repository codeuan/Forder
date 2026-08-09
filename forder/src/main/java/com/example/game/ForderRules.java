package com.example.game;

/**
 * Pure rules/bitboard helpers for Forder.
 *
 * Board encoding:
 *   RIGHT / Red  = 1
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


    private static final int[] COLUMN_MASKS = {
            0x1111,
            0x2222,
            0x4444,
            0x8888
    };

    private ForderRules() {
    }

    /** Returns the board obtained by flipping exactly one cell. */
    public static int flip(int board, int cell) {
        validateCell(cell);
        return board ^ (1 << cell);
    }

    /**
     * Kept for compatibility with any existing code/tests that specifically
     * want to ask about horizontal rows.
     */
    public static boolean hasWinningRow(int board, Player player) {
        return hasCompleteLine(board, player, ROW_MASKS);
    }


    public static boolean hasWinningColumn(int board, Player player) {
        return hasCompleteLine(board, player, COLUMN_MASKS);
    }


    public static boolean hasWinningLine(int board, Player player) {
        return hasWinningRow(board, player)
                || hasWinningColumn(board, player);
    }

    private static boolean hasCompleteLine(
            int board,
            Player player,
            int[] masks
    ) {
        for (int mask : masks) {
            if (player == Player.RED) {
                // Four RIGHT/Red cells => all four bits are 1.
                if ((board & mask) == mask) {
                    return true;
                }
            } else {
                // Four LEFT/Blue cells => all four bits are 0.
                if ((board & mask) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the winner on this board, or null when the board is non-terminal.
     *
     * A simultaneous Red and Blue win should not be reachable during normal
     * play because play stops as soon as the first winning line appears.
     */
    public static Player winner(int board) {

        boolean red = hasWinningLine(board, Player.RED);
        boolean blue = hasWinningLine(board, Player.BLUE);

        if (red && blue) {
            throw new IllegalStateException(
                    "Board contains simultaneous RED and BLUE winning lines. "
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
            throw new IllegalArgumentException(
                    "Cell must be in range 0..15: " + cell
            );
        }
    }
}
