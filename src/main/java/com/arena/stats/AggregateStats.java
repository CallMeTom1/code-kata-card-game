package com.arena.stats;

import java.util.Map;

/** Totals over N matches; rates are percentages, averages are per match. */
public record AggregateStats(int matches, int wins1, int wins2, int draws, int firstPlayerWins, long totalRounds,
                             long totalDamage1, long totalDamage2, Map<String, Integer> endReasons) {

    public double winRate1() {
        return percent(wins1);
    }

    public double winRate2() {
        return percent(wins2);
    }

    public double drawRate() {
        return percent(draws);
    }

    /** Share of matches won by whoever went first, to check that The Coin balances the start. */
    public double firstPlayerWinRate() {
        return percent(firstPlayerWins);
    }

    public double averageRounds() {
        return average(totalRounds);
    }

    public double averageDamage1() {
        return average(totalDamage1);
    }

    public double averageDamage2() {
        return average(totalDamage2);
    }

    private double percent(int count) {
        return matches == 0 ? 0 : 100.0 * count / matches;
    }

    private double average(long total) {
        return matches == 0 ? 0 : (double) total / matches;
    }
}
