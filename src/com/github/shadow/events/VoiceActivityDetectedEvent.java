package com.github.shadow.events;

public class VoiceActivityDetectedEvent {
    private double level;

    public VoiceActivityDetectedEvent(double level) {
        this.level = level;
    }

    public double getLevel() {
        return level;
    }
}