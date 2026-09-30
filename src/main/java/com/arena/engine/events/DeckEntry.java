package com.arena.engine.events;

/** One card of a deck as shown in the setup block of the log. */
public record DeckEntry(String name, int cost, String category) {
}
