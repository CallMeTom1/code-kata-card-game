package com.pokemonarena.deck;

import com.pokemonarena.cards.Card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A draw pile. Shuffling is the only source of randomness here and always goes through the
 * injected {@link Random}, so a seeded match is fully reproducible.
 */
public final class Deck {

    private final Deque<Card> cards;

    /** Creates a deck in the given draw order (first element is drawn first). */
    public Deck(List<Card> cards) {
        this.cards = new ArrayDeque<>(cards);
    }

    public static Deck shuffled(List<Card> cards, Random random) {
        List<Card> copy = new ArrayList<>(cards);
        Collections.shuffle(copy, random);
        return new Deck(copy);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /** Draws the top card, or empty when the deck is exhausted (there is no fatigue). */
    public Optional<Card> draw() {
        return Optional.ofNullable(cards.pollFirst());
    }
}
