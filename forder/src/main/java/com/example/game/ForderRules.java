package com.example.game;

/**
 * Pure rules/bitboard helpers for Forder.
 *
 * Board encoding:
 *   RIGHT / Red  = 1
 *   LEFT  / Blue = 0
 *
 * The 8x8 board uses all 64 bits of a Java long.
 */
public final class ForderRules {

    public static final int ROWS = 8;
    public static final int COLS = 8;
    public static final int CELLS = ROWS * COLS;

    /**
     * Official 8x8 starting board:
     *
     * R R R R L L L L
     * R R R R L L L L
     * R R R R L L L L
     * R R R R L L L L
     * L L L L R R R R
     * L L L L R R R R
     * L L L L R R R R
     * L L L L R R R R
     */
    public static final long START_BOARD = 0xF0F0F0F00F0F0F0FL;

    private static final long[] ROW_MASKS = buildRowMasks();
    private static final long[] COLUMN_MASKS = buildColumnMasks();

    private ForderRules() {
    }

    /** Returns the board obtained by flipping exactly one cell. */
    public static long flip(long board, int cell) {
        validateCell(cell);
        return board ^ (1L << cell);
    }

    public static boolean hasWinningRow(long board, Player player) {
        return hasCompleteLine(board, player, ROW_MASKS);
    }

    public static boolean hasWinningColumn(long board, Player player) {
        return hasCompleteLine(board, player, COLUMN_MASKS);
    }

    public static boolean hasWinningLine(long board, Player player) {
        return hasWinningRow(board, player)
                || hasWinningColumn(board, player);
    }

    private static boolean hasCompleteLine(
            long board,
            Player player,
            long[] masks
    ) {
        for (long mask : masks) {
            if (player == Player.RED) {
                // Eight RIGHT/Red cells => all eight bits in the line are 1.
                if ((board & mask) == mask) {
                    return true;
                }
            } else {
                // Eight LEFT/Blue cells => all eight bits in the line are 0.
                if ((board & mask) == 0L) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the winner on this board, or null when the board is non-terminal.
     */
    public static Player winner(long board) {
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

    public static boolean isTerminal(long board) {
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

    public static String boardToString(long board) {
        StringBuilder out = new StringBuilder();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cell = row * COLS + col;
                boolean right = (board & (1L << cell)) != 0L;
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

    private static long[] buildRowMasks() {
        long[] masks = new long[ROWS];

        for (int row = 0; row < ROWS; row++) {
            long mask = 0L;

            for (int col = 0; col < COLS; col++) {
                int cell = row * COLS + col;
                mask |= 1L << cell;
            }

            masks[row] = mask;
        }

        return masks;
    }

    private static long[] buildColumnMasks() {
        long[] masks = new long[COLS];

        for (int col = 0; col < COLS; col++) {
            long mask = 0L;

            for (int row = 0; row < ROWS; row++) {
                int cell = row * COLS + col;
                mask |= 1L << cell;
            }

            masks[col] = mask;
        }

        return masks;
    }

    private static void validateCell(int cell) {
        if (cell < 0 || cell >= CELLS) {
            throw new IllegalArgumentException(
                    "Cell must be in range 0.." + (CELLS - 1) + ": " + cell
            );
        }
    }
}
