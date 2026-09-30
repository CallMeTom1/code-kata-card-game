package com.arena.stats;

import com.arena.engine.match.MatchResult;

import java.util.ArrayList;
import java.util.List;

/** Accumulates match results and reduces them into {@link AggregateStats} for a batch run. */
public final class StatsCollector {

    private final String player1Name;
    private final List<MatchResult> results = new ArrayList<>();
    private final List<String> firstPlayers = new ArrayList<>();

    public StatsCollector(String player1Name) {
        this.player1Name = player1Name;
    }

    /** Records one finished match, and who went first in it. */
    public void record(MatchResult result, String firstPlayer) {
        results.add(result);
        firstPlayers.add(firstPlayer);
    }

    /** Reduces all recorded matches into win rates, draw rate and averages. */
    public AggregateStats summarize() {
        int player1Wins = 0;
        int player2Wins = 0;
        int draws = 0;
        int firstPlayerWins = 0;
        long totalTurns = 0;
        long totalDamage1 = 0;
        long totalDamage2 = 0;

        for (int i = 0; i < results.size(); i++) {
            MatchResult result = results.get(i);
            totalTurns += result.turns();
            totalDamage1 += result.damageByPlayer1();
            totalDamage2 += result.damageByPlayer2();
            if (result.winner() == null) {
                draws++;
            } else if (result.winner().equals(player1Name)) {
                player1Wins++;
            } else {
                player2Wins++;
            }
            if (result.winner() != null && result.winner().equals(firstPlayers.get(i))) {
                firstPlayerWins++;
            }
        }

        int matches = results.size();
        double averageTurns = matches == 0 ? 0 : (double) totalTurns / matches;
        double averageDamage1 = matches == 0 ? 0 : (double) totalDamage1 / matches;
        double averageDamage2 = matches == 0 ? 0 : (double) totalDamage2 / matches;
        return new AggregateStats(matches, player1Wins, player2Wins, draws, firstPlayerWins,
                averageTurns, averageDamage1, averageDamage2);
    }
}
