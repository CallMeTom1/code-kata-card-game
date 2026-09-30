package com.pokemonarena.deck;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The four predefined 20-card deck lists. Each list is fixed and deterministic: the only
 * randomness is the seeded shuffle performed by {@link Deck#shuffled}.
 */
public final class DeckLists {

    public static final int DECK_SIZE = 20;
    public static final int MAX_COPIES = 3;

    private static final Map<DeckId, List<Card>> LISTS = buildLists();

    private DeckLists() {
    }

    /** The cards of a deck, in their fixed definition order. */
    public static List<Card> cards(DeckId id) {
        return LISTS.get(id);
    }

    private static Map<DeckId, List<Card>> buildLists() {
        Map<DeckId, List<Card>> lists = new EnumMap<>(DeckId.class);
        lists.put(DeckId.AGGRO, aggro());
        lists.put(DeckId.CONTROL, control());
        lists.put(DeckId.ENERGY, energy());
        lists.put(DeckId.BALANCED, balanced());
        lists.replaceAll((id, cards) -> validated(id, cards));
        return Collections.unmodifiableMap(lists);
    }

    /** Fast damage, cheap Pokémon and reach from Attack Items. */
    private static List<Card> aggro() {
        List<Card> cards = new ArrayList<>();
        add(cards, CardCatalog.PIKACHU, 3);
        add(cards, CardCatalog.CHARMANDER, 3);
        add(cards, CardCatalog.PIDGEY, 2);
        add(cards, CardCatalog.BULBASAUR, 2);
        add(cards, CardCatalog.CHARMELEON, 1);
        add(cards, CardCatalog.ABRA, 1);
        add(cards, CardCatalog.THUNDER_SHOCK, 3);
        add(cards, CardCatalog.FIRE_BLAST, 2);
        add(cards, CardCatalog.ROCK_THROW, 2);
        add(cards, CardCatalog.ENERGY, 1);
        return cards;
    }

    /** Survive, heal and land big late Pokémon. */
    private static List<Card> control() {
        List<Card> cards = new ArrayList<>();
        add(cards, CardCatalog.SQUIRTLE, 2);
        add(cards, CardCatalog.ONIX, 3);
        add(cards, CardCatalog.CHANSEY, 2);
        add(cards, CardCatalog.SNORLAX, 2);
        add(cards, CardCatalog.BLASTOISE, 2);
        add(cards, CardCatalog.POTION, 2);
        add(cards, CardCatalog.SUPER_POTION, 2);
        add(cards, CardCatalog.HYPER_POTION, 1);
        add(cards, CardCatalog.DEFENSE_X, 2);
        add(cards, CardCatalog.PROTECTION, 2);
        return cards;
    }

    /** Ramp first, then drop the biggest threats. */
    private static List<Card> energy() {
        List<Card> cards = new ArrayList<>();
        add(cards, CardCatalog.ENERGY, 3);
        add(cards, CardCatalog.SUPER_BONBON, 3);
        add(cards, CardCatalog.POKE_BALL, 3);
        add(cards, CardCatalog.EEVEE, 2);
        add(cards, CardCatalog.SQUIRTLE, 2);
        add(cards, CardCatalog.CHARIZARD, 2);
        add(cards, CardCatalog.BLASTOISE, 2);
        add(cards, CardCatalog.CHARMELEON, 2);
        add(cards, CardCatalog.RAPPEL, 1);
        return cards;
    }

    /** A bit of every category, with a smooth curve. */
    private static List<Card> balanced() {
        List<Card> cards = new ArrayList<>();
        add(cards, CardCatalog.PIKACHU, 2);
        add(cards, CardCatalog.CHARMANDER, 2);
        add(cards, CardCatalog.SQUIRTLE, 2);
        add(cards, CardCatalog.EEVEE, 2);
        add(cards, CardCatalog.ONIX, 2);
        add(cards, CardCatalog.CHANSEY, 1);
        add(cards, CardCatalog.CHARMELEON, 1);
        add(cards, CardCatalog.CHARIZARD, 1);
        add(cards, CardCatalog.THUNDER_SHOCK, 2);
        add(cards, CardCatalog.ROCK_THROW, 1);
        add(cards, CardCatalog.POTION, 2);
        add(cards, CardCatalog.DEFENSE_X, 1);
        add(cards, CardCatalog.ENERGY, 1);
        return cards;
    }

    private static void add(List<Card> cards, Card card, int copies) {
        for (int i = 0; i < copies; i++) {
            cards.add(card);
        }
    }

    private static List<Card> validated(DeckId id, List<Card> cards) {
        if (cards.size() != DECK_SIZE) {
            throw new IllegalStateException(id + " must contain " + DECK_SIZE + " cards, not " + cards.size());
        }
        Map<String, Integer> copies = new HashMap<>();
        for (Card card : cards) {
            CardCatalog.byId(card.id());
            int count = copies.merge(card.id(), 1, Integer::sum);
            if (count > MAX_COPIES) {
                throw new IllegalStateException(id + " contains more than " + MAX_COPIES
                        + " copies of " + card.id());
            }
        }
        return List.copyOf(cards);
    }
}
