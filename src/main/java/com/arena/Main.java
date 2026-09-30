package com.arena;

import com.arena.cli.CommandLineOptions;
import com.arena.cli.MatchRunner;
import com.arena.stats.AggregateStats;

/** CLI entry point: `--matches N --p1 <Bot>:<Class> --p2 <Bot>:<Class> [--seed S] [--log]`. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("Skirmish Arena");
        CommandLineOptions options = CommandLineOptions.parse(args);
        AggregateStats stats = new MatchRunner().run(options);
        printSummary(stats);
    }

    private static void printSummary(AggregateStats stats) {
        System.out.printf("Matches: %d%n", stats.matches());
        System.out.printf("Player1 win rate: %.1f%%%n", stats.player1WinRate() * 100);
        System.out.printf("Player2 win rate: %.1f%%%n", stats.player2WinRate() * 100);
        System.out.printf("Draw rate: %.1f%%%n", stats.drawRate() * 100);
        System.out.printf("First player win rate: %.1f%%%n", stats.firstPlayerWinRate() * 100);
        System.out.printf("Average turns: %.1f%n", stats.averageTurns());
        System.out.printf("Average damage — Player1: %.1f, Player2: %.1f%n",
                stats.averageDamageByPlayer1(), stats.averageDamageByPlayer2());
    }
}
