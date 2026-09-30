package com.arena.engine.events;

/** A summon failed because the board already had 7 minions. */
public record SummonFizzled(String owner, String minion) implements GameEvent {
}
