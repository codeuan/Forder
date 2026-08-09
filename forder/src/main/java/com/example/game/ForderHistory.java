package com.example.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks the actual sequence of whole-board states seen in one Forder game.
 *
 * A cell may be flipped more than once, but a complete board position may not
 * be repeated.
 */
public final class ForderHistory {

    private final Set<Long> visited = new HashSet<>();
    private final List<Long> sequence = new ArrayList<>();

    public ForderHistory() {
        reset();
    }

    /** Resets history to the official starting board only. */
    public void reset() {
        visited.clear();
        sequence.clear();

        visited.add(ForderRules.START_BOARD);
        sequence.add(ForderRules.START_BOARD);
    }

    public boolean hasSeen(long board) {
        return visited.contains(board);
    }

    /**
     * Returns true if flipping this cell from currentBoard obeys the
     * no-repeated-board rule.
     */
    public boolean isLegalMove(long currentBoard, int cell) {
        if (ForderRules.isTerminal(currentBoard)) {
            return false;
        }

        long next = ForderRules.flip(currentBoard, cell);
        return !visited.contains(next);
    }

    /**
     * Records a board after a real move has been made.
     */
    public void recordBoard(long board) {
        if (!visited.add(board)) {
            throw new IllegalArgumentException(
                    "Illegal Forder move: board state has already occurred."
            );
        }

        sequence.add(board);
    }

    /**
     * Validates a move, records its resulting board and returns the new board.
     */
    public long playMove(long currentBoard, int cell) {
        if (!isLegalMove(currentBoard, cell)) {
            throw new IllegalArgumentException(
                    "Illegal Forder move at cell " + cell
                            + ": it repeats a previous board or the game is over."
            );
        }

        long next = ForderRules.flip(currentBoard, cell);
        recordBoard(next);
        return next;
    }

    /** Safe copy for AI search. */
    public Set<Long> copyVisited() {
        return new HashSet<>(visited);
    }

    public List<Long> sequence() {
        return Collections.unmodifiableList(sequence);
    }

    public int movesPlayed() {
        return sequence.size() - 1;
    }
}
