package com.arena.engine.events;

/** Published when a card moves from the deck to the hand during the draw phase. */
public record CardDrawn(String player, String card) implements GameEvent {
}
