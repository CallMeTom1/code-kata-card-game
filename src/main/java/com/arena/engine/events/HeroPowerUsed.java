package com.arena.engine.events;

/** A bot used its hero power. */
public record HeroPowerUsed(String player, String power, int cost, int manaLeft) implements GameEvent {
}
