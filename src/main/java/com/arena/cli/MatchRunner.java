package com.arena.cli;

import com.arena.engine.events.EventPublisher;
import com.arena.engine.match.Contender;
import com.arena.engine.match.Match;
import com.arena.engine.match.MatchResult;
import com.arena.log.ConsoleRenderer;
import com.arena.log.JsonEventExporter;
import com.arena.stats.MatchRecord;
import com.arena.stats.StatsAggregator;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Plays N matches with seeds S, S+1, …; only the first one is logged in full. */
public final class MatchRunner {

    private final MatchFactory factory;

    /** Takes the factory so tests could plug other contenders. */
    public MatchRunner(MatchFactory factory) {
        this.factory = factory;
    }

    /** Runs every match; per-match records are kept only when the stats are exported. */
    public RunReport run(CommandLineOptions options, PrintStream out) {
        StatsAggregator stats = new StatsAggregator();
        List<MatchRecord> records = new ArrayList<>();
        for (int i = 0; i < options.matches(); i++) {
            long seed = options.seed() + i;
            Contender p1 = factory.contender(options.names().get(0), options.player1(), options.presetDecks(), seed * 2);
            Contender p2 = factory.contender(options.names().get(1), options.player2(), options.presetDecks(),
                    seed * 2 + 1);
            EventPublisher events = new EventPublisher();
            if (i == 0 && options.log()) {
                events.subscribe(new ConsoleRenderer(out));
            }
            MatchResult result = i == 0 && options.jsonFile() != null
                    ? playWithJson(p1, p2, seed, events, options)
                    : new Match(p1, p2, seed, events).play();
            stats.add(result);
            if (options.statsJsonFile() != null) {
                records.add(new MatchRecord(seed, p1.heroClass().name(), p2.heroClass().name(), result));
            }
        }
        return new RunReport(stats.result(), records);
    }

    private MatchResult playWithJson(Contender p1, Contender p2, long seed, EventPublisher events,
                                     CommandLineOptions options) {
        try (Writer writer = Files.newBufferedWriter(options.jsonFile(), StandardCharsets.UTF_8)) {
            events.subscribe(new JsonEventExporter(writer));
            return new Match(p1, p2, seed, events).play();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + options.jsonFile(), e);
        }
    }
}
