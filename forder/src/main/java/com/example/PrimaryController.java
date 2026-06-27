package com.example;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class PrimaryController {

    @FXML
    private GridPane boardGrid;

    private static final int BOARD_SIZE = 9;
    private static final int CELL_SIZE = 55;

    private StackPane selectedCell = null;

    private static final String NORMAL_STYLE =
        "-fx-background-color: #f2e6c9;" +
        "-fx-border-color: black;" +
        "-fx-border-width: 1;";

    private static final String SELECTED_STYLE =
        "-fx-background-color: #8fbc8f;" +
        "-fx-border-color: black;" +
        "-fx-border-width: 1;";

    @FXML
    private void initialize() {
        createBoard();
    }

    private void createBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane cell = new StackPane();

                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle(NORMAL_STYLE);


                final int r = row;
                final int c = col;

                cell.setOnMouseClicked(event -> {
                    System.out.println("Clicked row " + r + ", column " + c);

                    if (selectedCell != null) {
                        selectedCell.setStyle(NORMAL_STYLE);
                    }

                    cell.setStyle(SELECTED_STYLE);
                    selectedCell = cell;
                });

                boardGrid.add(cell, col, row);
            }
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}