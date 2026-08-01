package com.example.game;

import com.example.game.Cell.Direction;

public class Player {

    private final String name;
    private final Direction direction;

    public Player(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }
}