package com.arena.stats;

import com.arena.engine.match.MatchResult;
import com.arena.json.Json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Exports a batch as JSON for the statistics tab of the web replay. */
public final class StatsJsonWriter {

    private StatsJsonWriter() {
    }

    /** Summary first, then one entry per match so the front can draw distributions. */
    public static String write(String player1, String label1, String player2, String label2, long firstSeed,
                               AggregateStats stats, List<MatchRecord> records) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("matches", stats.matches());
        summary.put("wins1", stats.wins1());
        summary.put("wins2", stats.wins2());
        summary.put("draws", stats.draws());
        summary.put("firstPlayerWins", stats.firstPlayerWins());
        summary.put("winRate1", round(stats.winRate1()));
        summary.put("winRate2", round(stats.winRate2()));
        summary.put("drawRate", round(stats.drawRate()));
        summary.put("firstPlayerWinRate", round(stats.firstPlayerWinRate()));
        summary.put("averageRounds", round(stats.averageRounds()));
        summary.put("averageDamage1", round(stats.averageDamage1()));
        summary.put("averageDamage2", round(stats.averageDamage2()));
        summary.put("endReasons", new TreeMap<>(stats.endReasons()));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("player1", player1);
        root.put("label1", label1);
        root.put("player2", player2);
        root.put("label2", label2);
        root.put("firstSeed", firstSeed);
        root.put("summary", summary);
        root.put("results", records.stream().map(StatsJsonWriter::entry).toList());
        return Json.write(root);
    }

    private static Map<String, Object> entry(MatchRecord record) {
        MatchResult r = record.result();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("seed", record.seed());
        entry.put("class1", record.class1());
        entry.put("class2", record.class2());
        entry.put("first", r.firstPlayer());
        entry.put("winner", r.winner());
        entry.put("reason", r.reason());
        entry.put("rounds", r.rounds());
        entry.put("hp1", r.hp1());
        entry.put("hp2", r.hp2());
        entry.put("damage1", r.damage1());
        entry.put("damage2", r.damage2());
        return entry;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
