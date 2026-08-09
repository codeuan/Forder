package com.example;

import com.example.game.Board;
import com.example.game.ForderRules;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class PrimaryController {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Button playAgainButton;

    private static final int BOARD_SIZE = ForderRules.ROWS;
    private static final int CELL_SIZE = 55;

    private Board board;

    @FXML
    public void initialize() {
        board = new Board(
                boardGrid,
                BOARD_SIZE,
                CELL_SIZE
        );

        board.setOnGameOver(() -> {
            playAgainButton.setVisible(true);
            playAgainButton.setManaged(true);
        });

        board.createBoard();
    }

    @FXML
    private void handlePlayAgain() {
        playAgainButton.setVisible(false);
        playAgainButton.setManaged(false);
        board.createBoard();
    }
}
