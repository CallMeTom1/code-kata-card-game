package com.arena.engine.match;

/** Who won and why; {@code winner} is null for a draw. */
public record Verdict(String winner, String reason) {

    /** True when nobody won. */
    public boolean isDraw() {
        return winner == null;
    }
}
