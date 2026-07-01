package com.example.game;

import javafx.scene.layout.StackPane;

public class Cell extends StackPane {

    private final int row;
    private final int col;

    private Player owner = null;

    private static final String EMPTY_COLOUR = "#f2e6c9";

    public Cell(int row, int col, int cellSize) {
        this.row = row;
        this.col = col;

        setPrefSize(cellSize, cellSize);
        updateStyle(false);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        updateStyle(false);
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isEmpty() {
        return owner == null;
    }

    public void select() {
        updateStyle(true);
    }

    public void deselect() {
        updateStyle(false);
    }

    private void updateStyle(boolean selected) {
        String backgroundColour;

        if (owner == null) {
            backgroundColour = EMPTY_COLOUR;
        } else {
            backgroundColour = owner.getColour();
        }

        String borderWidth = selected ? "3" : "1";

        setStyle(
            "-fx-background-color: " + backgroundColour + ";" +
            "-fx-border-color: black;" +
            "-fx-border-width: " + borderWidth + ";"
        );
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}