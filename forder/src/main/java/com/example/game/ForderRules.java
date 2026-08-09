package com.example.game;

/**
 * Pure rules/bitboard helpers for Forder.
 *
 * Board encoding:
 *   RIGHT / Red  = 1
 *   LEFT  / Blue = 0
 *
 * Cell numbering:
 *
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

    public static final int START_BOARD = 0xCC33;

    /*
     * Four horizontal lines:
     *
     * XXXX
     * ....
     * ....
     * ....
     *
     * etc.
     */
    private static final int[] ROW_MASKS = {
            0x000F,
            0x00F0,
            0x0F00,
            0xF000
    };

    /*
     * Four vertical lines:
     *
     * X...
     * X...
     * X...
     * X...
     *
     * etc.
     */
    private static final int[] COLUMN_MASKS = {
            0x1111,
            0x2222,
            0x4444,
            0x8888
    };

    private ForderRules() {
    }

    public static int flip(int board, int cell) {
        validateCell(cell);
        return board ^ (1 << cell);
    }

    /**
     * Returns true if the player owns any complete
     * horizontal OR vertical line.
     */
    public static boolean hasWinningLine(
            int board,
            Player player
    ) {
        return hasCompleteLine(board, player, ROW_MASKS)
                || hasCompleteLine(board, player, COLUMN_MASKS);
    }

    private static boolean hasCompleteLine(
            int board,
            Player player,
            int[] masks
    ) {
        for (int mask : masks) {

            if (player == Player.RED) {

                // Red/RIGHT = 1
                if ((board & mask) == mask) {
                    return true;
                }

            } else {

                // Blue/LEFT = 0
                if ((board & mask) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Player winner(int board) {

        boolean redWon = hasWinningLine(board, Player.RED);
        boolean blueWon = hasWinningLine(board, Player.BLUE);

        if (redWon && blueWon) {
            throw new IllegalStateException(
                    "Board contains simultaneous RED and BLUE winning lines."
            );
        }

        if (redWon) {
            return Player.RED;
        }

        if (blueWon) {
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

                boolean right =
                        (board & (1 << cell)) != 0;

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