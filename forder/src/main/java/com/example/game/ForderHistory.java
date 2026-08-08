package com.example.game;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Tracks the actual sequence of whole-board states seen in one Forder game.
 *
 * Important rule represented here:
 * - a cell MAY be flipped more than once;
 * - a whole board configuration may NOT be repeated.
 */
public final class ForderHistory {

    private final BitSet visited = new BitSet(ForderRules.BOARD_COUNT);
    private final List<Integer> sequence = new ArrayList<>();

    public ForderHistory() {
        reset();
    }

    /** Resets history to the official starting board only. */
    public void reset() {
        visited.clear();
        sequence.clear();
        visited.set(ForderRules.START_BOARD);
        sequence.add(ForderRules.START_BOARD);
    }

    public boolean hasSeen(int board) {
        return visited.get(board);
    }

    /**
     * Returns true if flipping this cell from currentBoard would obey the
     * no-repeated-board rule.
     */
    public boolean isLegalMove(int currentBoard, int cell) {
        if (ForderRules.isTerminal(currentBoard)) {
            return false;
        }

        int next = ForderRules.flip(currentBoard, cell);
        return !visited.get(next);
    }

    /**
     * Records a board after a real move has been made.
     * Throws if that board has already appeared in this game.
     */
    public void recordBoard(int board) {
        if (visited.get(board)) {
            throw new IllegalArgumentException(
                    "Illegal Forder move: board state has already occurred."
            );
        }

        visited.set(board);
        sequence.add(board);
    }

    /**
     * Convenience method: validate a move, record its resulting board, and
     * return the new board integer.
     */
    public int playMove(int currentBoard, int cell) {
        if (!isLegalMove(currentBoard, cell)) {
            throw new IllegalArgumentException(
                    "Illegal Forder move at cell " + cell
                            + ": it repeats a previous board or the game is over."
            );
        }

        int next = ForderRules.flip(currentBoard, cell);
        recordBoard(next);
        return next;
    }

    /** Safe clone for AI search; the AI can mutate this copy while exploring. */
    public BitSet copyVisited() {
        return (BitSet) visited.clone();
    }

    public List<Integer> sequence() {
        return Collections.unmodifiableList(sequence);
    }

    public int movesPlayed() {
        return sequence.size() - 1;
    }
}
