package com.example.game;

import com.example.game.Cell.Direction;

public enum Player {

    RED("Player 1", Direction.RIGHT),
    BLUE("Player 2", Direction.LEFT);

    private final String name;
    private final Direction direction;

    Player(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }


    public Player other() {
        return this == RED ? BLUE : RED;
    }
}
