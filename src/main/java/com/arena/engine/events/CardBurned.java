package com.arena.engine.events;

/** A card was drawn with a full hand (10) and destroyed, as in Hearthstone. */
public record CardBurned(String player, String card) implements GameEvent {
}
