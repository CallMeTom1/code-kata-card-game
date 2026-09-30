package com.pokemonarena.cli;

import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.bot.Bots;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.game.MatchListener;
import com.pokemonarena.hero.HeroCatalog;
import com.pokemonarena.logging.MatchLogger;
import com.pokemonarena.session.PlayerSetup;
import com.pokemonarena.simulation.Simulation;
import com.pokemonarena.simulation.SimulationConfig;
import com.pokemonarena.simulation.SimulationStats;

import java.util.Locale;

/**
 * AI vs AI simulation from the command line.
 * <pre>
 * --p1 hero:deck:bot   e.g. zapdos:aggro:aggressive   (default)
 * --p2 hero:deck:bot   e.g. articuno:control:defensive (default)
 * --matches N          (default 100)
 * --seed S             (default 42)
 * --log                print the turn-by-turn log of the first match
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | java.util.NoSuchElementException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Usage: --p1 hero:deck:bot --p2 hero:deck:bot [--matches N] [--seed S] [--log]");
            System.exit(1);
        }
    }

    static void run(String[] args) {
        String p1 = "zapdos:aggro:aggressive";
        String p2 = "articuno:control:defensive";
        int matches = 100;
        long seed = 42;
        boolean log = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--p1" -> p1 = value(args, ++i);
                case "--p2" -> p2 = value(args, ++i);
                case "--matches" -> matches = Integer.parseInt(value(args, ++i));
                case "--seed" -> seed = Long.parseLong(value(args, ++i));
                case "--log" -> log = true;
                default -> throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }
        String[] side1 = split(p1);
        String[] side2 = split(p2);
        SimulationConfig config = new SimulationConfig(setup(side1), bot(side1), setup(side2), bot(side2), seed, matches);
        MatchListener listener = log ? new MatchLogger(System.out) : MatchListener.NONE;
        SimulationStats stats = Simulation.run(config, listener);
        print(config, stats);
    }

    private static String value(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("missing value for " + args[index - 1]);
        }
        return args[index];
    }

    private static String[] split(String side) {
        String[] parts = side.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("expected hero:deck:bot, got " + side);
        }
        return parts;
    }

    private static PlayerSetup setup(String[] side) {
        return new PlayerSetup(HeroCatalog.byId(side[0].toLowerCase(Locale.ROOT)),
                DeckId.valueOf(side[1].toUpperCase(Locale.ROOT)));
    }

    private static BotStrategy bot(String[] side) {
        return Bots.byName(side[2]);
    }

    private static void print(SimulationConfig config, SimulationStats stats) {
        System.out.println();
        System.out.println("=== Simulation: " + stats.matches() + " matches, seed " + config.seed() + " ===");
        System.out.printf(Locale.ROOT, "P1 %s (%s): %d wins (%.1f%%), avg damage %.1f%n", config.side1(),
                config.bot1().name(), stats.winsSide1(), 100 * stats.winRateSide1(), stats.averageDamageSide1());
        System.out.printf(Locale.ROOT, "P2 %s (%s): %d wins (%.1f%%), avg damage %.1f%n", config.side2(),
                config.bot2().name(), stats.winsSide2(), 100 * stats.winRateSide2(), stats.averageDamageSide2());
        System.out.printf(Locale.ROOT, "Draws: %d (%.1f%%)%n", stats.draws(), 100 * stats.drawRate());
        System.out.printf(Locale.ROOT, "Average match length: %.1f turns%n", stats.averageTurns());
    }
}
