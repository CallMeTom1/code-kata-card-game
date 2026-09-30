package com.arena.cli;

import java.nio.file.Path;
import java.util.List;

/** Everything Main needs from the command line, validated in one place. */
public record CommandLineOptions(int matches, PlayerSpec player1, PlayerSpec player2, long seed, boolean log,
                                 boolean presetDecks, List<String> names, Path jsonFile, boolean help) {

    /** Printed on --help and after any wrong argument. */
    public static final String USAGE = """
            Usage: mvn -q compile exec:java -Dexec.args="[options]"
              --matches N            number of matches to simulate (default 100)
              --p1 <Bot>:<Class|auto> player 1 (default Aggressive:Mage)
              --p2 <Bot>:<Class|auto> player 2 (default Defensive:Tank)
                                     bots: Aggressive, Defensive, Random
                                     classes: Mage, Tank, Swordsman, Assassin, Cleric, or auto
              --seed S               seed of the first match; match i uses S + i (default 42)
              --log                  print the first match turn by turn
              --preset-decks         use the preset decks of DESIGN.md instead of bot-built decks
              --names A,B            player names (default P1,P2)
              --json FILE            also write the first match as JSON lines (for a future front)
              --help                 show this help""";

    /** Reads the arguments; throws with a clear message on anything unexpected. */
    public static CommandLineOptions parse(String[] args) {
        int matches = 100;
        PlayerSpec p1 = new PlayerSpec("Aggressive", "Mage");
        PlayerSpec p2 = new PlayerSpec("Defensive", "Tank");
        long seed = 42L;
        boolean log = false;
        boolean preset = false;
        List<String> names = List.of("P1", "P2");
        Path json = null;
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--matches" -> matches = positive(arg, value(args, ++i, arg));
                case "--p1" -> p1 = PlayerSpec.parse(value(args, ++i, arg));
                case "--p2" -> p2 = PlayerSpec.parse(value(args, ++i, arg));
                case "--seed" -> seed = number(arg, value(args, ++i, arg));
                case "--log" -> log = true;
                case "--preset-decks" -> preset = true;
                case "--names" -> names = names(value(args, ++i, arg));
                case "--json" -> json = Path.of(value(args, ++i, arg));
                case "--help", "-h" -> help = true;
                default -> throw new IllegalArgumentException("Unknown option " + arg);
            }
        }
        return new CommandLineOptions(matches, p1, p2, seed, log, preset, names, json, help);
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value after " + option);
        }
        return args[index];
    }

    private static long number(String option, String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(option + " expects a number, got '" + text + "'");
        }
    }

    private static int positive(String option, String text) {
        long value = number(option, text);
        if (value < 1 || value > 1_000_000) {
            throw new IllegalArgumentException(option + " must be between 1 and 1000000, got " + value);
        }
        return (int) value;
    }

    private static List<String> names(String text) {
        List<String> names = List.of(text.split(",", -1)).stream().map(String::trim).toList();
        if (names.size() != 2 || names.stream().anyMatch(String::isEmpty) || names.get(0).equals(names.get(1))) {
            throw new IllegalArgumentException("--names expects two different names like Alice,Bob");
        }
        return names;
    }
}
