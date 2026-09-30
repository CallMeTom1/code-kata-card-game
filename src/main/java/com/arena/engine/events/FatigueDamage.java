package com.arena.engine.events;

/** Drawing from an empty deck hurt; it grows by 1 each time. */
public record FatigueDamage(String player, int amount, int hpBefore, int hpAfter) implements GameEvent {
}
