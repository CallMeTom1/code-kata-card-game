package com.arena;

import com.arena.cli.CommandLineOptions;
import com.arena.cli.MatchFactory;
import com.arena.cli.MatchRunner;
import com.arena.stats.AggregateStats;
import com.arena.stats.StatsReport;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Entry point of the simulator: wires the concrete classes together (Dependency Inversion).
 */
public final class Main {

    private Main() {
    }

    /** Delegates to {@link #run} so the whole program can be tested without touching System.out. */
    public static void main(String[] args) {
        int code = run(args, new PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (code != 0) {
            System.exit(code);
        }
    }

    /** Runs the program against any output stream; returns 0 on success and 2 on a wrong argument. */
    public static int run(String[] args, PrintStream out) {
        CommandLineOptions options;
        MatchFactory factory = new MatchFactory();
        try {
            options = CommandLineOptions.parse(args);
            factory.check(options.player1());
            factory.check(options.player2());
        } catch (IllegalArgumentException e) {
            out.println("Error: " + e.getMessage());
            out.println(CommandLineOptions.USAGE);
            return 2;
        }
        if (options.help()) {
            out.println(CommandLineOptions.USAGE);
            return 0;
        }
        AggregateStats stats = new MatchRunner(factory).run(options, out);
        if (options.log()) {
            out.println();
        }
        String label1 = options.player1().bot() + ":" + options.player1().heroClass();
        String label2 = options.player2().bot() + ":" + options.player2().heroClass();
        out.println(StatsReport.format(stats, options.names().get(0), label1, options.names().get(1), label2,
                options.seed()));
        return 0;
    }
}
