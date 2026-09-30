package com.arena.cli;

import com.arena.engine.events.EventPublisher;
import com.arena.engine.match.Bot;
import com.arena.engine.match.Match;
import com.arena.engine.match.MatchResult;
import com.arena.engine.player.Champion;
import com.arena.log.ConsoleRenderer;
import com.arena.stats.AggregateStats;
import com.arena.stats.StatsCollector;

import java.util.Random;

/** Plays the requested number of matches, each with its own seed, and reduces them into stats. */
public final class MatchRunner {

    /** Runs the whole batch, logging only the first match in full when {@code options.log()} is true. */
    public AggregateStats run(CommandLineOptions options) {
        StatsCollector collector = new StatsCollector("Player1");
        for (int i = 0; i < options.matches(); i++) {
            long matchSeed = options.seed() + i;
            EventPublisher events = new EventPublisher();
            if (options.log() && i == 0) {
                events.subscribe(new ConsoleRenderer());
            }
            Random random = new Random(matchSeed);
            Champion player1 = new Champion("Player1", ClassFactory.create(options.class1()));
            Champion player2 = new Champion("Player2", ClassFactory.create(options.class2()));
            Bot bot1 = BotFactory.create(options.bot1(), random);
            Bot bot2 = BotFactory.create(options.bot2(), random);
            Match match = new Match(player1, bot1, player2, bot2, random, events);
            MatchResult result = match.play();
            collector.record(result, player1.name());
        }
        return collector.summarize();
    }
}
