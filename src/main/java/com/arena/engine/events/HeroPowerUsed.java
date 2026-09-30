package com.arena.engine.events;

/** Published when a champion spends mana to activate their once-per-turn hero power. */
public record HeroPowerUsed(String player, String heroPower, int manaLeft) implements GameEvent {
}
