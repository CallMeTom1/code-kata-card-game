package com.pokemonarena.game;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.hero.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Deterministic match builders shared by the engine tests. */
final class MatchFixture {

    private MatchFixture() {
    }

    /** Always makes player one start, so the turn order of the tests is explicit. */
    static Random playerOneStarts() {
        return new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }
        };
    }

    /** Pads a list of chosen cards with Pikachu up to a 20-card deck. */
    static List<Card> deck(List<Card> firstCards) {
        List<Card> cards = new ArrayList<>(firstCards);
        cards.addAll(Collections.nCopies(20 - firstCards.size(), CardCatalog.PIKACHU));
        return cards;
    }

    static Match match(Hero hero1, List<Card> deck1, Hero hero2, List<Card> deck2) {
        return new Match(hero1, new Deck(deck(deck1)), hero2, new Deck(deck(deck2)), playerOneStarts());
    }

    /** Plays empty turns until the given turn number, leaving the match in the PLAY phase. */
    static void advanceToTurn(Match match, int turnNumber) {
        while (match.turnNumber() < turnNumber - 1) {
            match.beginTurn();
            match.endTurn();
        }
        match.beginTurn();
        assertEquals(turnNumber, match.turnNumber());
    }
}
