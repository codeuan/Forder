package com.example;

import com.example.game.Board;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class PrimaryController {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Button playAgainButton;

    private static final int BOARD_SIZE = 4;
    private static final int CELL_SIZE = 55;

    private Board board;

    @FXML
    public void initialize() {
        this.board = new Board(boardGrid, statusLabel, playAgainButton, BOARD_SIZE, CELL_SIZE);
        board.createBoard();
    
    }


}