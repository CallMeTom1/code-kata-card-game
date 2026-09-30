package com.arena.engine.events;

/** Published once, when a champion dies or the turn limit is reached; carries the final outcome. */
public record MatchEnded(String winner, String reason, int turns) implements GameEvent {
}
