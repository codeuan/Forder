package com.example;

import com.example.game.Board;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class PrimaryController {

    @FXML
    private GridPane boardGrid;

    private static final int BOARD_SIZE = 4;
    private static final int CELL_SIZE = 90;

    private Board board;

    @FXML
    public void initialize() {

        board = new Board(
            boardGrid,
            BOARD_SIZE,
            CELL_SIZE
        );

        board.createBoard();
    }


}