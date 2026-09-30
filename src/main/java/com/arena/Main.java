package com.arena;

import com.arena.bots.llm.ClaudeLlmClient;
import com.arena.bots.llm.LlmClient;
import com.arena.cli.CommandLineOptions;
import com.arena.cli.EnvFile;
import com.arena.cli.LlmSettings;
import com.arena.cli.MatchFactory;
import com.arena.cli.MatchRunner;
import com.arena.cli.RunReport;
import com.arena.server.ArenaServer;
import com.arena.stats.StatsJsonWriter;
import com.arena.stats.StatsReport;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        MatchFactory factory;
        LlmSettings settings;
        try {
            options = CommandLineOptions.parse(args);
            settings = LlmSettings.from(EnvFile.load(Path.of("."), System.getenv()), options.llmModel());
            factory = new MatchFactory(claude(settings));
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
        if (options.servePort() > 0) {
            return serve(options.servePort(), factory, settings, out);
        }
        RunReport report = new MatchRunner(factory).run(options, out);
        if (options.log()) {
            out.println();
        }
        String label1 = options.player1().bot() + ":" + options.player1().heroClass();
        String label2 = options.player2().bot() + ":" + options.player2().heroClass();
        out.println(StatsReport.format(report.stats(), options.names().get(0), label1, options.names().get(1), label2,
                options.seed()));
        if (options.jsonFile() != null) {
            out.println("[EXPORT ] first match written to " + options.jsonFile());
        }
        if (options.statsJsonFile() != null) {
            String json = StatsJsonWriter.write(options.names().get(0), label1, options.names().get(1), label2,
                    options.seed(), report.stats(), report.records());
            try {
                Files.writeString(options.statsJsonFile(), json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                out.println("Error: cannot write " + options.statsJsonFile() + ": " + e.getMessage());
                return 1;
            }
            out.println("[EXPORT ] stats written to " + options.statsJsonFile());
        }
        return 0;
    }

    /** Runs the live server until the program is stopped (Ctrl+C). */
    private static int serve(int port, MatchFactory factory, LlmSettings settings, PrintStream out) {
        try {
            ArenaServer server = new ArenaServer(port, Path.of("web"), factory, settings.hasApiKey());
            server.start();
            out.println("[SERVER ] Skirmish Arena live on http://localhost:" + server.port() + " (Ctrl+C to stop)");
            out.println("[SERVER ] Llm bots: " + (settings.hasApiKey() ? "ready (" + settings.model() + ")"
                    : "no API key (put ANTHROPIC_API_KEY in .env.local)"));
            Thread.currentThread().join();
            return 0;
        } catch (IOException e) {
            out.println("Error: cannot start the server on port " + port + ": " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /** One Claude client per run, created only if an Llm bot plays (classic runs need no key). */
    private static java.util.function.Supplier<LlmClient> claude(LlmSettings settings) {
        LlmClient[] client = new LlmClient[1];
        return () -> {
            if (client[0] == null) {
                client[0] = new ClaudeLlmClient(settings.apiKey(), settings.model());
            }
            return client[0];
        };
    }
}
