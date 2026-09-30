package com.arena.testing;

import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;

import java.util.ArrayList;
import java.util.List;

/** Test fake that just remembers every event it receives, in order. */
public final class RecordingListener implements GameEventListener {

    private final List<GameEvent> received = new ArrayList<>();

    @Override
    public void on(GameEvent event) {
        received.add(event);
    }

    public List<GameEvent> received() {
        return received;
    }
}
