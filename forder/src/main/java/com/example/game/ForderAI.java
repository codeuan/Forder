package com.example.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * On-the-fly Forder AI using iterative deepening, minimax, alpha-beta pruning,
 * move ordering, heuristic evaluation and complete visited-board history.
 *
 * No transposition table is used because the same visible board can have
 * different legal moves under different histories.
 */
public final class ForderAI {

    private static final int WIN_SCORE = 1_000_000;
    private static final int HEURISTIC_CAP = WIN_SCORE - 1_000;

    /**
     * Value of having N cells of one player in a row/column.
     * Index 8 is zero because an 8-cell line is terminal and is scored by
     * WIN_SCORE before the heuristic is reached.
     */
    private static final int[] LINE_WEIGHTS = {
        0,
        1,
        4,
        16,
        64,
        256,
        1_024,
        8_192,
        0
    };

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
     * @param board           current 64-bit board
     * @param history         actual game history
     * @param playerToMove    whose turn it is now
     * @param maxDepth        deepest full minimax iteration to attempt
     * @param timeLimitMillis thinking time; use <= 0 for no time limit
     */
    public SearchResult chooseMove(
            long board,
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

    /** Same search API when the caller already owns a set of visited boards. */
    public SearchResult chooseMove(
            long board,
            Set<Long> history,
            Player playerToMove,
            int maxDepth,
            long timeLimitMillis
    ) {
        if (playerToMove != aiPlayer) {
            throw new IllegalArgumentException(
                    "This ForderAI plays " + aiPlayer
                            + " but chooseMove was called on "
                            + playerToMove + "'s turn."
            );
        }

        if (maxDepth < 1) {
            throw new IllegalArgumentException(
                    "maxDepth must be at least 1."
            );
        }

        Player existingWinner = ForderRules.winner(board);
        if (existingWinner != null) {
            return new SearchResult(
                    -1,
                    terminalScore(existingWinner, 0),
                    0,
                    0,
                    false
            );
        }

        Set<Long> visited = new HashSet<>(history);
        visited.add(board);

        List<Integer> rootMoves = legalMoves(board, visited);
        if (rootMoves.isEmpty()) {
            return new SearchResult(-1, 0, 0, 0, false);
        }

        orderMoves(rootMoves, board, playerToMove);

        // Always have a legal fallback even if the first iteration times out.
        int bestMove = rootMoves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int completedDepth = 0;
        boolean timedOut = false;

        nodesSearched = 0;
        deadlineNanos = timeLimitMillis <= 0
                ? Long.MAX_VALUE
                : System.nanoTime() + timeLimitMillis * 1_000_000L;

        // Never miss an immediate winning move before deeper search starts.
        for (int cell : rootMoves) {
            long next = ForderRules.flip(board, cell);
            Player winner = ForderRules.winner(next);

            if (winner == aiPlayer) {
                return new SearchResult(
                        cell,
                        WIN_SCORE,
                        1,
                        nodesSearched,
                        false
                );
            }
        }

        // Keep the move from the deepest COMPLETED iteration.
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

                // Only terminal search scores can reach WIN_SCORE.
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
            long board,
            Set<Long> visited,
            Player playerToMove,
            int depth
    ) {
        checkTime();

        List<Integer> moves = legalMoves(board, visited);
        orderMoves(moves, board, playerToMove);

        int bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int cell : moves) {
            checkTime();

            long next = ForderRules.flip(board, cell);
            visited.add(next);

            int score;
            try {
                score = minimax(
                        next,
                        visited,
                        playerToMove.other(),
                        depth - 1,
                        alpha,
                        beta
                );
            } finally {
                visited.remove(next);
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = cell;
            }

            alpha = Math.max(alpha, bestScore);
        }

        return new RootResult(bestMove, bestScore);
    }

    private int minimax(
            long board,
            Set<Long> visited,
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

        orderMoves(moves, board, playerToMove);

        if (playerToMove == aiPlayer) {
            int value = Integer.MIN_VALUE;

            for (int cell : moves) {
                long next = ForderRules.flip(board, cell);
                visited.add(next);

                try {
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
                } finally {
                    visited.remove(next);
                }

                alpha = Math.max(alpha, value);

                if (alpha >= beta) {
                    break;
                }
            }

            return value;
        }

        int value = Integer.MAX_VALUE;

        for (int cell : moves) {
            long next = ForderRules.flip(board, cell);
            visited.add(next);

            try {
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
            } finally {
                visited.remove(next);
            }

            beta = Math.min(beta, value);

            if (alpha >= beta) {
                break;
            }
        }

        return value;
    }

    /** Heuristic score from the AI's point of view. */
    private int evaluate(
            long board,
            Set<Long> visited,
            Player playerToMove
    ) {
        int ownImmediateWins = countImmediateWinningMoves(
                board,
                visited,
                aiPlayer
        );

        int opponentImmediateWins = countImmediateWinningMoves(
                board,
                visited,
                aiPlayer.other()
        );

        int score = 0;

        // Immediate tactical threats dominate ordinary line shape.
        score += 12_000 * ownImmediateWins;
        score -= 12_000 * opponentImmediateWins;

        // Forks: multiple distinct one-move wins are especially dangerous.
        if (ownImmediateWins >= 2) {
            score += 20_000 * (ownImmediateWins - 1);
        }

        if (opponentImmediateWins >= 2) {
            score -= 20_000 * (opponentImmediateWins - 1);
        }

        if (playerToMove == aiPlayer && ownImmediateWins > 0) {
            score += 15_000;
        }

        if (playerToMove != aiPlayer && opponentImmediateWins > 0) {
            score -= 15_000;
        }

        int ownPotential = positionalPotential(board, aiPlayer);
        int opponentPotential = positionalPotential(board, aiPlayer.other());

        score += ownPotential - opponentPotential;

        // Heuristic values must never masquerade as proven terminal scores.
        return Math.max(-HEURISTIC_CAP, Math.min(HEURISTIC_CAP, score));
    }

    /**
     * Counts legal single-cell flips that immediately create a winning
     * horizontal or vertical line for the specified player.
     */
    private int countImmediateWinningMoves(
            long board,
            Set<Long> visited,
            Player player
    ) {
        int count = 0;

        for (int cell = 0; cell < ForderRules.CELLS; cell++) {
            long next = ForderRules.flip(board, cell);

            if (visited.contains(next)) {
                continue;
            }

            Player winner = ForderRules.winner(next);
            if (winner == player) {
                count++;
            }
        }

        return count;
    }

    private int positionalPotential(long board, Player player) {
        int potential = 0;

        // Horizontal lines.
        for (int row = 0; row < ForderRules.ROWS; row++) {
            int redCount = 0;

            for (int col = 0; col < ForderRules.COLS; col++) {
                int cell = row * ForderRules.COLS + col;

                if ((board & (1L << cell)) != 0L) {
                    redCount++;
                }
            }

            int playerCount = player == Player.RED
                    ? redCount
                    : ForderRules.COLS - redCount;

            potential += LINE_WEIGHTS[playerCount];
        }

        // Vertical lines.
        for (int col = 0; col < ForderRules.COLS; col++) {
            int redCount = 0;

            for (int row = 0; row < ForderRules.ROWS; row++) {
                int cell = row * ForderRules.COLS + col;

                if ((board & (1L << cell)) != 0L) {
                    redCount++;
                }
            }

            int playerCount = player == Player.RED
                    ? redCount
                    : ForderRules.ROWS - redCount;

            potential += LINE_WEIGHTS[playerCount];
        }

        return potential;
    }

    private List<Integer> legalMoves(long board, Set<Long> visited) {
        List<Integer> moves = new ArrayList<>(ForderRules.CELLS);

        for (int cell = 0; cell < ForderRules.CELLS; cell++) {
            long next = ForderRules.flip(board, cell);

            if (!visited.contains(next)) {
                moves.add(cell);
            }
        }

        return moves;
    }

    /**
     * Good move ordering lets alpha-beta prune more aggressively.
     */
    private void orderMoves(
            List<Integer> moves,
            long board,
            Player playerToMove
    ) {
        Comparator<Integer> comparator = Comparator.comparingInt(
                cell -> quickOrderingScore(board, cell)
        );

        if (playerToMove == aiPlayer) {
            comparator = comparator.reversed();
        }

        moves.sort(comparator);
    }

    private int quickOrderingScore(long board, int cell) {
        long next = ForderRules.flip(board, cell);
        Player winner = ForderRules.winner(next);

        if (winner == aiPlayer) {
            return WIN_SCORE;
        }

        if (winner == aiPlayer.other()) {
            return -WIN_SCORE;
        }

        int ownPotential = positionalPotential(next, aiPlayer);
        int opponentPotential = positionalPotential(next, aiPlayer.other());

        return ownPotential - opponentPotential;
    }

    private int terminalScore(Player winner, int depthRemaining) {
        if (winner == aiPlayer) {
            // Prefer faster wins.
            return WIN_SCORE + depthRemaining;
        }

        // Prefer slower losses.
        return -WIN_SCORE - depthRemaining;
    }

    private void checkTime() {
        if (System.nanoTime() >= deadlineNanos) {
            throw SearchTimeout.INSTANCE;
        }
    }
}
