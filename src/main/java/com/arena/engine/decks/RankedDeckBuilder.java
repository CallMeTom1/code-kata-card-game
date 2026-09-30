package com.arena.engine.decks;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.classes.HeroClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Builds a legal deck from a preference order, so each strategy only says what it likes.
 * It fills class cards first (at least 6), then Attack cards up to a minimum, then the rest.
 */
public final class RankedDeckBuilder {

    private final List<Card> deck = new ArrayList<>();
    private final Map<String, Integer> copies = new HashMap<>();

    private RankedDeckBuilder() {
    }

    /**
     * @param preference best cards first
     * @param allowed    cards the strategy wants; others are used only if the deck cannot be filled
     * @param minAttacks minimum Attack cards, so a defensive deck can still win
     */
    public static List<Card> build(HeroClass heroClass, List<Card> neutralCards, Comparator<Card> preference,
                                   Predicate<Card> allowed, int minAttacks) {
        RankedDeckBuilder builder = new RankedDeckBuilder();
        List<Card> classCards = heroClass.classCards().stream().sorted(preference).toList();
        List<Card> everything = Stream.concat(classCards.stream(), neutralCards.stream()).sorted(preference).toList();
        List<Card> wanted = everything.stream().filter(allowed).toList();

        builder.fillUntil(classCards.stream().filter(allowed).toList(),
                () -> builder.count(classCards::contains) >= DeckValidator.MIN_CLASS_CARDS);
        builder.fillUntil(classCards, () -> builder.count(classCards::contains) >= DeckValidator.MIN_CLASS_CARDS);
        builder.fillUntil(wanted.stream().filter(c -> c.category() == CardCategory.ATTACK).toList(),
                () -> builder.count(c -> c.category() == CardCategory.ATTACK) >= minAttacks);
        builder.fillUntil(wanted, builder::isFull);
        builder.fillUntil(everything, builder::isFull);
        return List.copyOf(builder.deck);
    }

    private void fillUntil(List<Card> candidates, BooleanSupplier done) {
        for (Card card : candidates) {
            if (done.getAsBoolean()) {
                return;
            }
            while (!isFull() && copies.getOrDefault(card.name(), 0) < DeckValidator.MAX_COPIES) {
                deck.add(card);
                copies.merge(card.name(), 1, Integer::sum);
            }
        }
    }

    private long count(Predicate<Card> filter) {
        return deck.stream().filter(filter).count();
    }

    private boolean isFull() {
        return deck.size() >= DeckValidator.DECK_SIZE;
    }
}
