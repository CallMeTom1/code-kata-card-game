package com.arena.engine.events;

/** Closes the log and feeds the stats; {@code winner} is "DRAW" when nobody won. */
public record MatchEnded(String winner, String reason, int turns) implements GameEvent {
}
