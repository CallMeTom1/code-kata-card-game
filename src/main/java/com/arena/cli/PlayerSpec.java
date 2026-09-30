package com.arena.cli;

/** One side as typed on the command line: "Aggressive:Mage" or "Random:auto". */
public record PlayerSpec(String bot, String heroClass) {

    /** Value meaning "let the bot choose its class". */
    public static final String AUTO = "auto";

    /** Parses "Bot" or "Bot:Class"; a missing class means auto. */
    public static PlayerSpec parse(String text) {
        String[] parts = text.split(":", 2);
        if (parts[0].isBlank()) {
            throw new IllegalArgumentException("Expected <Bot>:<Class|auto>, got '" + text + "'");
        }
        return new PlayerSpec(parts[0], parts.length == 2 && !parts[1].isBlank() ? parts[1] : AUTO);
    }

    /** True when the bot picks its own class. */
    public boolean autoClass() {
        return AUTO.equalsIgnoreCase(heroClass);
    }
}
