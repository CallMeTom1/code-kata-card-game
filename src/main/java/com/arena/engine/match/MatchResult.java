package com.arena.engine.match;

/** Everything the stats need from one match. {@code winner} is null for a draw. */
public record MatchResult(String player1, String player2, String firstPlayer, String winner, String reason,
                          int rounds, int hp1, int hp2, int damage1, int damage2) {

    /** True when nobody won. */
    public boolean isDraw() {
        return winner == null;
    }
}
