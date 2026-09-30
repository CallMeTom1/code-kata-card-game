package com.arena.engine.events;

/** A minion lost health. */
public record MinionDamaged(String owner, String minion, String source, int amount, int healthLeft)
        implements GameEvent {
}
