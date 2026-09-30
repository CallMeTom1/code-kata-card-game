package com.arena.engine.events;

/** Published when a drawn card is discarded because the hand was already at its 10-card limit. */
public record CardBurned(String player, String card) implements GameEvent {
}
