package com.example.game;

import com.example.game.Cell.Direction;

public final class PatternEngine {

    private PatternEngine() {
    }

    public static boolean hasWinningLine(
            Cell[][] cells,
            Direction direction
    ) {
        int board = ForderBoardConverter.encode(cells);

        Player player = direction == Direction.RIGHT
                ? Player.RED
                : Player.BLUE;

        return ForderRules.hasWinningLine(
                board,
                player
        );
    }
}