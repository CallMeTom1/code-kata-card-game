package com.arena.engine.events;

/** A Resource card changed mana or crystals; shows the new values. */
public record ManaGained(String player, String source, int mana, int maxMana) implements GameEvent {
}
