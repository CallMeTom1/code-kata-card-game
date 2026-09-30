package com.arena.engine.events;

/** A card went from the deck to the hand. */
public record CardDrawn(String player, String card) implements GameEvent {
}
