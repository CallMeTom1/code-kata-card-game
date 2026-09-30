package com.arena.engine.events;

/** Opens the log: who plays which bot and class, and the seed to replay the match. */
public record MatchStarted(String player1, String player1Label, String player2, String player2Label, long seed) implements GameEvent {
}
