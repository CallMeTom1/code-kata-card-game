package com.arena.engine.events;

import java.util.ArrayList;
import java.util.List;

/** Fan-outs events to every subscribed listener so the engine never needs to know who listens. */
public final class EventPublisher {

    private final List<GameEventListener> listeners = new ArrayList<>();

    /** Registers a listener; it will receive every event published afterward. */
    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    /** Notifies all listeners, in subscription order, so replays stay deterministic. */
    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.on(event);
        }
    }
}
