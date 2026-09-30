package com.arena.testing;

import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;

import java.util.ArrayList;
import java.util.List;

/** Test fake that keeps every event it receives, so tests can assert on what the engine published. */
public final class RecordingListener implements GameEventListener {

    private final List<GameEvent> events = new ArrayList<>();

    @Override
    public void on(GameEvent event) {
        events.add(event);
    }

    /** Gives tests a read-only view so they cannot tamper with the recording. */
    public List<GameEvent> events() {
        return List.copyOf(events);
    }

    /** Lets a test focus on one kind of event without filtering by hand. */
    public <T extends GameEvent> List<T> eventsOfType(Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }
}
