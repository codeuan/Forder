package com.example.game;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

/**
 * On-the-fly Forder AI using:
 * - iterative deepening;
 * - minimax;
 * - alpha-beta pruning;
 * - a heuristic evaluation at the search frontier;
 * - the complete visited-board history for move legality.
 *
 * No transposition table is used because the same visible board can have
 * different legal moves under different histories.
 */
public final class ForderAI {

    private static final int WIN_SCORE = 1_000_000;
    private static final int[] ROW_WEIGHTS = {0, 2, 18, 180, 0};

    private final Player aiPlayer;

    private long deadlineNanos;
    private long nodesSearched;

    public ForderAI(Player aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    public Player player() {
        return aiPlayer;
    }

    /**
     * Search result for diagnostics/UI.
     *
     * cell == -1 means there is no legal move.
     */
    public record SearchResult(
            int cell,
            int score,
            int completedDepth,
            long nodesSearched,
            boolean timeLimitReached
    ) {
        public int row() {
            return cell < 0 ? -1 : ForderRules.rowOf(cell);
        }

        public int col() {
            return cell < 0 ? -1 : ForderRules.colOf(cell);
        }
    }

    private record RootResult(int cell, int score) {
    }

    /** Lightweight internal exception used to abort an unfinished iteration. */
    private static final class SearchTimeout extends RuntimeException {
        private static final SearchTimeout INSTANCE = new SearchTimeout();

        private SearchTimeout() {
            super(null, null, false, false);
        }
    }

    /**
     * Chooses a move for this AI.
     *
     * @param board           current 16-bit board
     * @param history         actual game history (must include the current board;
     *                        this method defensively sets it if omitted)
     * @param playerToMove    whose turn it is now
     * @param maxDepth        deepest full minimax iteration to attempt
     * @param timeLimitMillis thinking time; use <= 0 for no time limit
     */
    public SearchResult chooseMove(
            int board,
            ForderHistory history,
            Player playerToMove,
            int maxDepth,
            long timeLimitMillis
    ) {
        return chooseMove(
                board,
                history.copyVisited(),
                playerToMove,
                maxDepth,
                timeLimitMillis
        );
    }

    /** Same search API when the caller already owns a BitSet of visited boards. */
    public SearchResult chooseMove(
            int board,
            BitSet history,
            Player playerToMove,
            int maxDepth,
            long timeLimitMillis
    ) {
        if (playerToMove != aiPlayer) {
            throw new IllegalArgumentException(
                    "This ForderAI plays " + aiPlayer
                            + " but chooseMove was called on " + playerToMove + "'s turn."
            );
        }

        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1.");
        }

        Player existingWinner = ForderRules.winner(board);
        if (existingWinner != null) {
            return new SearchResult(-1, terminalScore(existingWinner, 0), 0, 0, false);
        }

        BitSet visited = (BitSet) history.clone();
        visited.set(board);

        List<Integer> rootMoves = legalMoves(board, visited);
        if (rootMoves.isEmpty()) {
            return new SearchResult(-1, 0, 0, 0, false);
        }

        // Always have a legal fallback even if a tiny time limit expires early.
        int bestMove = rootMoves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int completedDepth = 0;
        boolean timedOut = false;

        nodesSearched = 0;
        deadlineNanos = timeLimitMillis <= 0
                ? Long.MAX_VALUE
                : System.nanoTime() + timeLimitMillis * 1_000_000L;

        // Immediate winning moves should never be missed, even before deep search.
        for (int cell : rootMoves) {
            int next = ForderRules.flip(board, cell);
            Player winner = ForderRules.winner(next);
            if (winner == aiPlayer) {
                return new SearchResult(cell, WIN_SCORE, 1, nodesSearched, false);
            }
        }

        // Iterative deepening: keep the result from the deepest COMPLETED search.
        for (int depth = 1; depth <= maxDepth; depth++) {
            try {
                RootResult result = searchRoot(
                        board,
                        visited,
                        playerToMove,
                        depth
                );

                bestMove = result.cell();
                bestScore = result.score();
                completedDepth = depth;

                // A proven forced win within this horizon cannot be improved in outcome.
                if (bestScore >= WIN_SCORE) {
                    break;
                }

            } catch (SearchTimeout ignored) {
                timedOut = true;
                break;
            }
        }

        return new SearchResult(
                bestMove,
                bestScore,
                completedDepth,
                nodesSearched,
                timedOut
        );
    }

    private RootResult searchRoot(
            int board,
            BitSet visited,
            Player playerToMove,
            int depth
    ) {
        checkTime();

        List<Integer> moves = legalMoves(board, visited);
        orderMoves(moves, board, visited, playerToMove);

        int bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int cell : moves) {
            checkTime();

            int next = ForderRules.flip(board, cell);
            visited.set(next);

            int score = minimax(
                    next,
                    visited,
                    playerToMove.other(),
                    depth - 1,
                    alpha,
                    beta
            );

            visited.clear(next);

            if (score > bestScore) {
                bestScore = score;
                bestMove = cell;
            }

            alpha = Math.max(alpha, bestScore);
        }

        return new RootResult(bestMove, bestScore);
    }

    private int minimax(
            int board,
            BitSet visited,
            Player playerToMove,
            int depth,
            int alpha,
            int beta
    ) {
        nodesSearched++;
        checkTime();

        Player winner = ForderRules.winner(board);
        if (winner != null) {
            return terminalScore(winner, depth);
        }

        List<Integer> moves = legalMoves(board, visited);

        // No unused neighbouring board remains: treat as a draw.
        if (moves.isEmpty()) {
            return 0;
        }

        if (depth == 0) {
            return evaluate(board, visited, playerToMove);
        }

        orderMoves(moves, board, visited, playerToMove);

        if (playerToMove == aiPlayer) {
            int value = Integer.MIN_VALUE;

            for (int cell : moves) {
                int next = ForderRules.flip(board, cell);
                visited.set(next);

                value = Math.max(
                        value,
                        minimax(
                                next,
                                visited,
                                playerToMove.other(),
                                depth - 1,
                                alpha,
                                beta
                        )
                );

                visited.clear(next);

                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }

            return value;

        } else {
            int value = Integer.MAX_VALUE;

            for (int cell : moves) {
                int next = ForderRules.flip(board, cell);
                visited.set(next);

                value = Math.min(
                        value,
                        minimax(
                                next,
                                visited,
                                playerToMove.other(),
                                depth - 1,
                                alpha,
                                beta
                        )
                );

                visited.clear(next);

                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break;
                }
            }

            return value;
        }
    }

    /** Heuristic score from the AI's point of view. */
    private int evaluate(
            int board,
            BitSet visited,
            Player playerToMove
    ) {
        int ownImmediateWins = countImmediateWinningMoves(board, visited, aiPlayer);
        int opponentImmediateWins = countImmediateWinningMoves(board, visited, aiPlayer.other());

        int score = 0;

        // Immediate tactical threats dominate ordinary row shape.
        score += 12_000 * ownImmediateWins;
        score -= 12_000 * opponentImmediateWins;

        // Forks: two or more distinct one-move wins are particularly dangerous.
        if (ownImmediateWins >= 2) {
            score += 20_000 * (ownImmediateWins - 1);
        }
        if (opponentImmediateWins >= 2) {
            score -= 20_000 * (opponentImmediateWins - 1);
        }

        // Make a one-move threat more urgent if that side is actually to move now.
        if (playerToMove == aiPlayer && ownImmediateWins > 0) {
            score += 15_000;
        }
        if (playerToMove != aiPlayer && opponentImmediateWins > 0) {
            score -= 15_000;
        }

        // Positional row potential.
        int redPotential = 0;
        int bluePotential = 0;

        for (int row = 0; row < ForderRules.ROWS; row++) {
            int redCount = 0;

            for (int col = 0; col < ForderRules.COLS; col++) {
                int cell = row * ForderRules.COLS + col;
                if ((board & (1 << cell)) != 0) {
                    redCount++;
                }
            }

            int blueCount = ForderRules.COLS - redCount;
            redPotential += ROW_WEIGHTS[redCount];
            bluePotential += ROW_WEIGHTS[blueCount];
        }

        if (aiPlayer == Player.RED) {
            score += redPotential - bluePotential;
        } else {
            score += bluePotential - redPotential;
        }

        return score;
    }

    /**
     * Counts legal single-cell flips that would immediately create a winning
     * row for the specified player.
     */
    private int countImmediateWinningMoves(
            int board,
            BitSet visited,
            Player player
    ) {
        int count = 0;

        for (int cell = 0; cell < ForderRules.CELLS; cell++) {
            int next = ForderRules.flip(board, cell);

            if (visited.get(next)) {
                continue;
            }

            Player winner = ForderRules.winner(next);
            if (winner == player) {
                count++;
            }
        }

        return count;
    }

    private List<Integer> legalMoves(int board, BitSet visited) {
        List<Integer> moves = new ArrayList<>(ForderRules.CELLS);

        for (int cell = 0; cell < ForderRules.CELLS; cell++) {
            int next = ForderRules.flip(board, cell);
            if (!visited.get(next)) {
                moves.add(cell);
            }
        }

        return moves;
    }

    /**
     * Good move ordering makes alpha-beta prune much more aggressively.
     * We score each child cheaply from the AI's perspective, then search the
     * likely-best move first for the side whose turn it is.
     */
    private void orderMoves(
            List<Integer> moves,
            int board,
            BitSet visited,
            Player playerToMove
    ) {
        Comparator<Integer> comparator = Comparator.comparingInt(
                cell -> quickOrderingScore(board, visited, cell)
        );

        if (playerToMove == aiPlayer) {
            comparator = comparator.reversed();
        }

        moves.sort(comparator);
    }

    private int quickOrderingScore(int board, BitSet visited, int cell) {
        int next = ForderRules.flip(board, cell);
        Player winner = ForderRules.winner(next);

        if (winner == aiPlayer) {
            return WIN_SCORE;
        }
        if (winner == aiPlayer.other()) {
            return -WIN_SCORE;
        }

        // A cheap positional estimate only; avoid recursively calling evaluate().
        int redPotential = 0;
        int bluePotential = 0;

        for (int row = 0; row < ForderRules.ROWS; row++) {
            int redCount = 0;

            for (int col = 0; col < ForderRules.COLS; col++) {
                int index = row * ForderRules.COLS + col;
                if ((next & (1 << index)) != 0) {
                    redCount++;
                }
            }

            redPotential += ROW_WEIGHTS[redCount];
            bluePotential += ROW_WEIGHTS[ForderRules.COLS - redCount];
        }

        return aiPlayer == Player.RED
                ? redPotential - bluePotential
                : bluePotential - redPotential;
    }

    private int terminalScore(Player winner, int depthRemaining) {
        if (winner == aiPlayer) {
            // Prefer faster wins: an earlier win leaves more unused search depth.
            return WIN_SCORE + depthRemaining;
        }

        // Prefer slower losses: a later loss leaves less unused search depth.
        return -WIN_SCORE - depthRemaining;
    }

    private void checkTime() {
        if (System.nanoTime() >= deadlineNanos) {
            throw SearchTimeout.INSTANCE;
        }
    }
}
