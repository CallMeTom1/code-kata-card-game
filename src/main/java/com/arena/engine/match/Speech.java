package com.arena.engine.match;

/** What a bot thought (shown in logs) and said aloud (heard by the opponent); either may be empty. */
public record Speech(String thought, String message) {

    /** Null-safe, so a bot can pass through whatever an LLM returned. */
    public Speech {
        thought = thought == null ? "" : thought.strip();
        message = message == null ? "" : message.strip();
    }
}
