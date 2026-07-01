package com.example.game;

public class Player {

    // The player's display name, e.g. "Player 1"
    private final String name;

    // The colour used for this player's cells.
    // This is a CSS colour string because JavaFX styling uses CSS.
    private final String colour;

    // This controls whether the player is allowed to move right now.
    // Only one player should have canMove = true at a time.
    private boolean canMove;

    public Player(String name, String colour, boolean canMove) {
        this.name = name;
        this.colour = colour;
        this.canMove = canMove;
    }

    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }

    public boolean canMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
    
}