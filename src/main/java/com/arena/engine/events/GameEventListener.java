package com.arena.engine.events;

/** Receives every event the engine publishes; console log, stats and a future front all implement this. */
public interface GameEventListener {

    /** Called synchronously for each event, in publication order. */
    void on(GameEvent event);
}
