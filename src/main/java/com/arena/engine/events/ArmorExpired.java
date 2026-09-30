package com.arena.engine.events;

/** Published when a timed armor charge reaches the start of its expiry turn and is removed. */
public record ArmorExpired(String player, int amount) implements GameEvent {
}
