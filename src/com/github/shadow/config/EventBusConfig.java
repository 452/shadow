package com.github.shadow.config;

import de.greenrobot.event.EventBus;

public class EventBusConfig {

    private static EventBus eventBus = EventBus.getDefault();

    public static EventBus getInstance() {
        return eventBus;
    }

}