package com.arena.engine.events;

/** Armor added, with how long it lasts and the new total. */
public record ArmorGained(String player, String source, int amount, int turns, int totalArmor) implements GameEvent {
}
