package com.arena.engine.classes;

import com.arena.engine.cards.Card;

import java.util.ArrayList;
import java.util.List;

/** Helper to write preset decks the way DESIGN.md lists them: "every card twice". */
final class Decks {

    private Decks() {
    }

    /** Two copies of each given card, in order. */
    static List<Card> twiceEach(List<Card> cards) {
        List<Card> deck = new ArrayList<>();
        for (Card card : cards) {
            deck.add(card);
            deck.add(card);
        }
        return deck;
    }

    /** Class cards twice each, then neutral cards twice each. */
    static List<Card> preset(List<Card> classCards, List<Card> neutrals) {
        List<Card> deck = twiceEach(classCards);
        deck.addAll(twiceEach(neutrals));
        return deck;
    }
}
