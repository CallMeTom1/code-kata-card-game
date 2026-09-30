package com.pokemonarena.server;

import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.game.MatchListener;

import java.util.ArrayList;
import java.util.List;

/** Collects the events of a match and hands each of them out once. */
public final class EventBuffer implements MatchListener {

    private final List<MatchEvent> events = new ArrayList<>();

    @Override
    public synchronized void onEvent(MatchEvent event) {
        events.add(event);
    }

    /** Returns the events received since the previous call and forgets them. */
    public synchronized List<MatchEvent> drain() {
        List<MatchEvent> drained = List.copyOf(events);
        events.clear();
        return drained;
    }
}
