package com.arena.cli;

/** Parses `--matches N --p1 Bot:Class --p2 Bot:Class [--seed S] [--log]` into typed options. */
public record CommandLineOptions(int matches, String bot1, String class1, String bot2, String class2,
                                  long seed, boolean log) {

    public static CommandLineOptions parse(String[] args) {
        int matches = 1;
        String bot1 = "Random";
        String class1 = "Mage";
        String bot2 = "Random";
        String class2 = "Tank";
        long seed = System.nanoTime();
        boolean log = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--matches" -> matches = Integer.parseInt(args[++i]);
                case "--p1" -> {
                    String[] parts = args[++i].split(":", 2);
                    bot1 = parts[0];
                    class1 = parts.length > 1 ? parts[1] : class1;
                }
                case "--p2" -> {
                    String[] parts = args[++i].split(":", 2);
                    bot2 = parts[0];
                    class2 = parts.length > 1 ? parts[1] : class2;
                }
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--log" -> log = true;
                default -> { /* ignore unknown flags, keep the CLI forgiving */ }
            }
        }
        return new CommandLineOptions(matches, bot1, class1, bot2, class2, seed, log);
    }
}
