package com.example.game;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class Cell extends StackPane {

    public enum Direction {
        LEFT,
        RIGHT
    }

    private final int row;
    private final int col;

    private Direction direction;

    private final Label label = new Label();

    public Cell(int row, int col, int size, Direction direction) {
        this.row = row;
        this.col = col;
        this.direction = direction;

        setPrefSize(size, size);

        getChildren().add(label);

        updateDisplay();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        updateDisplay();
    }

    public void flip() {
        if (direction == Direction.LEFT) {
            direction = Direction.RIGHT;
        } else {
            direction = Direction.LEFT;
        }

        updateDisplay();
    }

    private void updateDisplay() {

        String backgroundColour;

        if (direction == Direction.RIGHT) {
            label.setText("R");
            backgroundColour = "red";
        } else {
            label.setText("L");
            backgroundColour = "blue";
        }

        label.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;"
        );

        setStyle(
            "-fx-background-color: " + backgroundColour + ";" +
            "-fx-border-color: white;" +
            "-fx-border-width: 1;"
        );
    }
}