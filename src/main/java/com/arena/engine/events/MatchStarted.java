package com.arena.engine.events;

/** Published once, right after decks are shuffled and the opening hands are dealt. */
public record MatchStarted(String player1, String player2, long seed, String firstPlayer) implements GameEvent {
}
