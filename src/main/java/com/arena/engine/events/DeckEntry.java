package com.arena.engine.events;

/** One card of a deck as shown in the setup block of the log, with its rules text for the web replay. */
public record DeckEntry(String name, int cost, String category, String text) {

    /** For renderers and tests that do not need the rules text. */
    public DeckEntry(String name, int cost, String category) {
        this(name, cost, category, "");
    }
}
