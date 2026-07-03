package com.example.game;

import javafx.scene.layout.StackPane;

public class Cell extends StackPane {

    public enum CellState {
        EMPTY,
        RED,
        BLUE,
        EITHER
    }

    private final int row;
    private final int col;

    private CellState state = CellState.EMPTY;
    private Player owner = null;

    private boolean selected = false;

    public Cell(int row, int col, int size) {
        this.row = row;
        this.col = col;

        setPrefSize(size, size);
        updateStyle();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;

        // Empty and Either cells do not belong to a player.
        if (state == CellState.EMPTY || state == CellState.EITHER) {
            owner = null;
        }

        updateStyle();
    }

    public boolean isEmpty() {
        return state == CellState.EMPTY;
    }

    public boolean isEither() {
        return state == CellState.EITHER;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player player) {
        this.owner = player;

        if (player.getColour().equals("red")) {
            state = CellState.RED;
        } else if (player.getColour().equals("blue")) {
            state = CellState.BLUE;
        }

        updateStyle();
    }

    public void select() {
        selected = true;
        updateStyle();
    }

    public void deselect() {
        selected = false;
        updateStyle();
    }

    private void updateStyle() {
        String backgroundColour;

        switch (state) {
            case RED:
                backgroundColour = "red";
                break;

            case BLUE:
                backgroundColour = "blue";
                break;

            case EITHER:
                backgroundColour = "purple";
                break;

            case EMPTY:
            default:
                backgroundColour = "white";
                break;
        }

        String borderWidth = selected ? "4" : "1";

        setStyle(
            "-fx-border-color: black;" +
            "-fx-border-width: " + borderWidth + ";" +
            "-fx-background-color: " + backgroundColour + ";"
        );
    }
}