package com.arena.cli;

import com.arena.bots.AggressiveBot;
import com.arena.bots.AggressiveDeckStrategy;
import com.arena.bots.DefensiveBot;
import com.arena.bots.DefensiveDeckStrategy;
import com.arena.bots.RandomBot;
import com.arena.bots.RandomDeckStrategy;
import com.arena.engine.cards.Card;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.classes.StandardClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.DeckValidator;
import com.arena.engine.match.Bot;
import com.arena.engine.match.Contender;
import com.arena.engine.match.Seeds;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/** Wires a command-line spec to a bot, a class and a validated deck (the only place that knows them all). */
public final class MatchFactory {

    private static final List<String> BOTS = List.of("Aggressive", "Defensive", "Random");

    private final HeroClasses classes = StandardClasses.all();
    private final DeckValidator validator = new DeckValidator(NeutralCards.all());

    /** Builds one side; {@code seed} drives the Random bot and Random deck so matches stay replayable. */
    public Contender contender(String name, PlayerSpec spec, boolean presetDecks, long seed) {
        Random random = Seeds.random(seed);
        Bot bot = bot(spec.bot(), random);
        DeckStrategy strategy = deckStrategy(spec.bot(), random);
        HeroClass heroClass = spec.autoClass() ? strategy.chooseClass(classes) : heroClass(spec.heroClass());
        List<Card> deck = presetDecks ? heroClass.presetDeck() : strategy.buildDeck(heroClass, NeutralCards.all());
        List<String> errors = validator.validate(heroClass, deck);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid deck for " + name + ": " + errors);
        }
        return new Contender(name, bot, heroClass, deck, spec.autoClass() ? "auto" : "imposed",
                presetDecks ? "preset" : "built");
    }

    /** Fails fast on typos before any match runs. */
    public void check(PlayerSpec spec) {
        bot(spec.bot(), new Random(0));
        if (!spec.autoClass()) {
            heroClass(spec.heroClass());
        }
    }

    private Bot bot(String name, Random random) {
        return switch (canonical(name)) {
            case "Aggressive" -> new AggressiveBot();
            case "Defensive" -> new DefensiveBot();
            default -> new RandomBot(random);
        };
    }

    private DeckStrategy deckStrategy(String name, Random random) {
        return switch (canonical(name)) {
            case "Aggressive" -> new AggressiveDeckStrategy();
            case "Defensive" -> new DefensiveDeckStrategy();
            default -> new RandomDeckStrategy(random);
        };
    }

    private String canonical(String name) {
        return BOTS.stream().filter(bot -> bot.equalsIgnoreCase(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown bot '" + name + "'. Known bots: "
                        + String.join(", ", BOTS)));
    }

    private HeroClass heroClass(String name) {
        return classes.byName(name).orElseThrow(() -> new IllegalArgumentException("Unknown class '" + name
                + "'. Known classes: " + classes.all().stream().map(HeroClass::name).collect(Collectors.joining(", "))
                + " (or auto)"));
    }
}
