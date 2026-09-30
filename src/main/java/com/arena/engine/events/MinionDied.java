package com.arena.engine.events;

/** A minion reached 0 health and left the board. */
public record MinionDied(String owner, String minion) implements GameEvent {
}
