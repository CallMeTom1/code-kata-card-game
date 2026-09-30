package com.arena.cli;

import com.arena.bots.AggressiveBot;
import com.arena.bots.DefensiveBot;
import com.arena.bots.RandomBot;
import com.arena.engine.match.Bot;

import java.util.Random;

/** Creates a {@link Bot} by name; adding a new bot only means adding one case here (Open/Closed). */
public final class BotFactory {

    private BotFactory() {
    }

    public static Bot create(String name, Random random) {
        return switch (name) {
            case "Aggressive" -> new AggressiveBot();
            case "Defensive" -> new DefensiveBot();
            default -> new RandomBot(random);
        };
    }
}
