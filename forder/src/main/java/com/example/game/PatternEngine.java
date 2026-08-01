package com.example.game;

import com.example.game.Cell.Direction;

public class PatternEngine {

    public static boolean hasWinningLine(
            Cell[][] cells,
            Direction direction) {

        int size = cells.length;

        // Horizontal rows
        for (int row = 0; row < size; row++) {

            boolean completeRow = true;

            for (int col = 0; col < size; col++) {
                if (cells[row][col].getDirection() != direction) {
                    completeRow = false;
                    break;
                }
            }

            if (completeRow) {
                return true;
            }
        }

        // Vertical columns
        for (int col = 0; col < size; col++) {

            boolean completeColumn = true;

            for (int row = 0; row < size; row++) {
                if (cells[row][col].getDirection() != direction) {
                    completeColumn = false;
                    break;
                }
            }

            if (completeColumn) {
                return true;
            }
        }

        return false;
    }
}