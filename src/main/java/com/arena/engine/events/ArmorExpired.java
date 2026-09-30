package com.arena.engine.events;

/** Armor that ran out of turns. */
public record ArmorExpired(String player, int amount, int armorLeft) implements GameEvent {
}
