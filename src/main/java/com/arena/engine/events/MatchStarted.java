package com.arena.engine.events;

/** Opens the log with who plays, the seed to replay the match, and who goes first. */
public record MatchStarted(String player1, String player2, long seed, String firstPlayer) implements GameEvent {
}
