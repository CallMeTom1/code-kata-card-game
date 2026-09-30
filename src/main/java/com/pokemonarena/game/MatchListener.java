package com.pokemonarena.game;

/** Observes a match. Listeners must never mutate the game state. */
@FunctionalInterface
public interface MatchListener {

    MatchListener NONE = event -> {
    };

    void onEvent(MatchEvent event);
}
