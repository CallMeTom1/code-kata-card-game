package com.arena.stats;

import com.arena.engine.match.MatchResult;

import java.util.Map;
import java.util.TreeMap;

/** Accumulates match results; kept apart from rendering so numbers can be tested alone. */
public final class StatsAggregator {

    private final Map<String, Integer> endReasons = new TreeMap<>();
    private int matches;
    private int wins1;
    private int wins2;
    private int draws;
    private int firstPlayerWins;
    private long rounds;
    private long damage1;
    private long damage2;

    /** Adds one finished match. */
    public void add(MatchResult result) {
        matches++;
        rounds += result.rounds();
        damage1 += result.damage1();
        damage2 += result.damage2();
        endReasons.merge(result.reason(), 1, Integer::sum);
        if (result.isDraw()) {
            draws++;
            return;
        }
        if (result.winner().equals(result.player1())) {
            wins1++;
        } else {
            wins2++;
        }
        if (result.winner().equals(result.firstPlayer())) {
            firstPlayerWins++;
        }
    }

    /** Snapshot of the totals so far. */
    public AggregateStats result() {
        return new AggregateStats(matches, wins1, wins2, draws, firstPlayerWins, rounds, damage1, damage2,
                Map.copyOf(endReasons));
    }
}
