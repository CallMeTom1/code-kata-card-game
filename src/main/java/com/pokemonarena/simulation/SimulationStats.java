package com.pokemonarena.simulation;

import com.pokemonarena.game.Match;
import com.pokemonarena.game.MatchResult;

/** Aggregate statistics of a batch of matches, accumulated from finished matches only. */
public final class SimulationStats {

    private int matches;
    private int winsSide1;
    private int winsSide2;
    private int draws;
    private long totalTurns;
    private long damageBySide1;
    private long damageBySide2;

    void record(Match match) {
        MatchResult result = match.result();
        matches++;
        switch (result.outcome()) {
            case PLAYER_ONE_WINS -> winsSide1++;
            case PLAYER_TWO_WINS -> winsSide2++;
            case DRAW -> draws++;
        }
        totalTurns += result.turns();
        damageBySide1 += match.player1().damageDealtToOpposingHero();
        damageBySide2 += match.player2().damageDealtToOpposingHero();
    }

    public int matches() {
        return matches;
    }

    public int winsSide1() {
        return winsSide1;
    }

    public int winsSide2() {
        return winsSide2;
    }

    public int draws() {
        return draws;
    }

    public double winRateSide1() {
        return ratio(winsSide1);
    }

    public double winRateSide2() {
        return ratio(winsSide2);
    }

    public double drawRate() {
        return ratio(draws);
    }

    public double averageTurns() {
        return ratio(totalTurns);
    }

    /** Average damage dealt to the opposing Hero per match. */
    public double averageDamageSide1() {
        return ratio(damageBySide1);
    }

    public double averageDamageSide2() {
        return ratio(damageBySide2);
    }

    private double ratio(long value) {
        return matches == 0 ? 0 : (double) value / matches;
    }
}
