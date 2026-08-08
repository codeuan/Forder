package com.example.game;

/** Simple command-line smoke test for the AI core. */
public final class ForderAITest {

    private ForderAITest() {
    }

    public static void main(String[] args) {
        int board = ForderRules.START_BOARD;
        ForderHistory history = new ForderHistory();

        ForderAI redAI = new ForderAI(Player.RED);

        System.out.println("Starting board:");
        System.out.println(ForderRules.boardToString(board));
        System.out.println();

        ForderAI.SearchResult result = redAI.chooseMove(
                board,
                history,
                Player.RED,
                8,      // maximum depth attempted
                1000    // 1 second thinking time
        );

        System.out.println("AI move cell: " + result.cell());
        System.out.println("row=" + result.row() + ", col=" + result.col());
        System.out.println("score=" + result.score());
        System.out.println("completed depth=" + result.completedDepth());
        System.out.println("nodes searched=" + result.nodesSearched());
        System.out.println("time limit reached=" + result.timeLimitReached());

        if (result.cell() >= 0) {
            board = history.playMove(board, result.cell());
            System.out.println();
            System.out.println("Board after AI move:");
            System.out.println(ForderRules.boardToString(board));
        }
    }
}
