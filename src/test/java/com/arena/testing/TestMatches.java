package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroPower;
import com.arena.engine.effects.Effect;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.match.Bot;
import com.arena.engine.match.CoinFlip;
import com.arena.engine.match.Contender;
import com.arena.engine.match.Match;

import java.util.Collections;
import java.util.List;

/** Builds small matches for engine tests: fixed decks, a chosen first player, a test class. */
public final class TestMatches {

    private TestMatches() {
    }

    /** A class whose hero power is the given effect, with no class cards. */
    public static HeroClass testClass(Effect heroPower) {
        return new HeroClass("Tester", new HeroPower("Test Power", 2, heroPower), List.of(), List.of());
    }

    /** A deck made of the same card, so the hand content is known whatever the shuffle. */
    public static List<Card> deckOf(Card card, int size) {
        return Collections.nCopies(size, card);
    }

    public static Contender contender(String name, Bot bot, List<Card> deck) {
        return new Contender(name, bot, testClass(Effect.NONE), deck, "imposed", "preset");
    }

    public static Contender contender(String name, Bot bot, HeroClass heroClass, List<Card> deck) {
        return new Contender(name, bot, heroClass, deck, "imposed", "preset");
    }

    /** A match where player 1 always goes first. */
    public static Match p1First(Contender p1, Contender p2, EventPublisher events) {
        return new Match(p1, p2, 42L, events, CoinFlip.PLAYER_1_FIRST);
    }
}
