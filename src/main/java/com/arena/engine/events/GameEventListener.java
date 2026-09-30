package com.arena.engine.events;

/** Lets the console log, the stats and a future front observe a match without the engine knowing them. */
@FunctionalInterface
public interface GameEventListener {

    /** Called for every event, in the order the engine published them. */
    void on(GameEvent event);
}
