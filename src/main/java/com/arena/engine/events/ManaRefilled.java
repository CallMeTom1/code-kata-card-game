package com.arena.engine.events;

/** Published at the mana phase, once max mana has grown (capped at 10) and mana has been refilled. */
public record ManaRefilled(String player, int maxMana) implements GameEvent {
}
