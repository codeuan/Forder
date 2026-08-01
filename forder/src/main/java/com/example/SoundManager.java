package com.example;

import javafx.scene.media.AudioClip;

public class SoundManager {

    private static final AudioClip CLICK_SOUND = loadSound("/com/example/sounds/selectsquare.wav");
    private static final AudioClip GAME_START_SOUND = loadSound("/com/example/sounds/startgame.wav");

    private static AudioClip loadSound(String path) {
        var resource = SoundManager.class.getResource(path);

        if (resource == null) {
            System.err.println("Sound not found: " + path);
            return null;
        }

        return new AudioClip(resource.toExternalForm());
    }

    public static void playClick() {
        if (CLICK_SOUND != null) {
            CLICK_SOUND.play();
        }
    }

    public static void playGameStart() {
        if (GAME_START_SOUND != null) {
            GAME_START_SOUND.play();
        }
    }
}