package com.arena.engine.events;

/** Published instead of a draw when the deck is empty; damage grows by 1 each time it happens. */
public record FatigueDamage(String player, int amount, int hpLeft) implements GameEvent {
}
