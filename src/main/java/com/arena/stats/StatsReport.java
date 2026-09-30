package com.arena.stats;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Formats the aggregate stats with the same bracket style as the match log. */
public final class StatsReport {

    private StatsReport() {
    }

    /** Human-readable summary of N matches. */
    public static String format(AggregateStats stats, String name1, String label1, String name2, String label2,
                                long firstSeed) {
        String reasons = new TreeMap<>(stats.endReasons()).entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> e.getKey() + " " + pct(100.0 * e.getValue() / stats.matches()))
                .collect(Collectors.joining(" | "));
        return String.join(System.lineSeparator(),
                "=== Skirmish Arena — " + stats.matches() + " matches | " + name1 + " [" + label1 + "] vs " + name2
                        + " [" + label2 + "] | seeds " + firstSeed + ".." + (firstSeed + stats.matches() - 1) + " ===",
                row(name1 + " win rate", pct(stats.winRate1()) + " (" + stats.wins1() + ")"),
                row(name2 + " win rate", pct(stats.winRate2()) + " (" + stats.wins2() + ")"),
                row("Draws", pct(stats.drawRate()) + " (" + stats.draws() + ")"),
                row("First player wins", pct(stats.firstPlayerWinRate())),
                row("Avg match length", one(stats.averageRounds()) + " turns"),
                row("Avg damage / match", name1 + " " + one(stats.averageDamage1()) + " | " + name2 + " "
                        + one(stats.averageDamage2()) + " | total "
                        + one(stats.averageDamage1() + stats.averageDamage2())),
                row("End reasons", reasons));
    }

    private static String row(String label, String value) {
        return String.format("[STATS  ] %-19s: %s", label, value);
    }

    private static String pct(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    private static String one(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
