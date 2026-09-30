package com.pokemonarena.simulation;

import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.session.PlayerSetup;

import java.util.Objects;

/**
 * AI vs AI configuration. Match {@code i} (0-based) uses the seed {@code seed + i}, so a whole
 * batch is reproducible and any single match can be replayed alone.
 */
public record SimulationConfig(PlayerSetup side1, BotStrategy bot1,
                               PlayerSetup side2, BotStrategy bot2,
                               long seed, int matches) {

    public SimulationConfig {
        Objects.requireNonNull(side1, "side1");
        Objects.requireNonNull(bot1, "bot1");
        Objects.requireNonNull(side2, "side2");
        Objects.requireNonNull(bot2, "bot2");
        if (matches < 1) {
            throw new IllegalArgumentException("at least one match is required, not " + matches);
        }
    }

    public long seedOf(int matchIndex) {
        return seed + matchIndex;
    }
}
