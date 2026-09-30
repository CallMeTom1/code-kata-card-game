package com.pokemonarena.game;

/**
 * Outcome of a finished match.
 *
 * @param outcome which side won, if any
 * @param turns   number of individual Hero turns played
 * @param reason  human-readable explanation, used by the match log
 */
public record MatchResult(Outcome outcome, int turns, String reason) {

    public enum Outcome {
        PLAYER_ONE_WINS,
        PLAYER_TWO_WINS,
        DRAW
    }

    public boolean isDraw() {
        return outcome == Outcome.DRAW;
    }
}
