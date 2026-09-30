package com.arena.engine.decks;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Checks the deck rules of DESIGN.md before a match, so a bad deck builder fails loudly. */
public final class DeckValidator {

    /** Deck size required by the brief. */
    public static final int DECK_SIZE = 20;
    /** Copy limit, as in Hearthstone. */
    public static final int MAX_COPIES = 2;
    /** Minimum class cards, so every deck keeps its class identity. */
    public static final int MIN_CLASS_CARDS = 6;

    private final Set<String> neutralNames;

    /** Takes the neutral pool from outside so tests can use any card list. */
    public DeckValidator(List<Card> neutralCards) {
        this.neutralNames = neutralCards.stream().map(Card::name).collect(Collectors.toSet());
    }

    /** Returns every broken rule; an empty list means the deck is legal. */
    public List<String> validate(HeroClass heroClass, List<Card> deck) {
        List<String> errors = new ArrayList<>();
        if (deck.size() != DECK_SIZE) {
            errors.add("A deck needs exactly 20 cards, found " + deck.size());
        }
        Set<String> classNames = heroClass.classCards().stream().map(Card::name).collect(Collectors.toSet());
        Map<String, Long> copies = deck.stream()
                .collect(Collectors.groupingBy(Card::name, TreeMap::new, Collectors.counting()));
        copies.forEach((name, count) -> {
            if (count > MAX_COPIES) {
                errors.add(name + " has " + count + " copies, max " + MAX_COPIES);
            }
            if (!classNames.contains(name) && !neutralNames.contains(name)) {
                errors.add(name + " is not allowed in a " + heroClass.name() + " deck");
            }
        });
        long classCards = deck.stream().filter(card -> classNames.contains(card.name())).count();
        if (classCards < MIN_CLASS_CARDS) {
            errors.add("A deck needs at least 6 class cards, found " + classCards);
        }
        return errors;
    }
}
