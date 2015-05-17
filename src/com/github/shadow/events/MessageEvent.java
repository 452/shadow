package com.github.shadow.events;

public class MessageEvent {

    private final String data;

    public MessageEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
