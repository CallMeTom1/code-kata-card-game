package com.pokemonarena.bot;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/** Looks up the available Bot strategies by name (case-insensitive), e.g. for the CLI. */
public final class Bots {

    private static final List<Supplier<BotStrategy>> ALL = List.of(AggressiveBot::new, DefensiveBot::new);

    private Bots() {
    }

    public static List<String> names() {
        return ALL.stream().map(supplier -> supplier.get().name()).toList();
    }

    public static BotStrategy byName(String name) {
        return ALL.stream()
                .map(Supplier::get)
                .filter(bot -> bot.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("unknown bot: " + name + ", expected one of " + names()));
    }
}
