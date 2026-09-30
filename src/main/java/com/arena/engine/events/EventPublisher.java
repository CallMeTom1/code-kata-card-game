package com.arena.engine.events;

import java.util.ArrayList;
import java.util.List;

/** Single place where the engine reports what happens, so it never has to print anything itself. */
public final class EventPublisher {

    private final List<GameEventListener> listeners = new ArrayList<>();

    /** Adds an observer; several can watch the same match (log, stats, export). */
    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    /** Forwards the event to every observer in subscription order, so logs stay deterministic. */
    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.on(event);
        }
    }
}
