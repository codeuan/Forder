package com.example;

import java.io.IOException;

import com.example.game.Board;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class PrimaryController {

    @FXML
    private GridPane boardGrid;

    private static final int BOARD_SIZE = 10;
    private static final int CELL_SIZE = 55;

    private Board board;


    @FXML
    public void initialize() {
        Board board = new Board(boardGrid, BOARD_SIZE, CELL_SIZE);
        board.createBoard();
        
    }
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}