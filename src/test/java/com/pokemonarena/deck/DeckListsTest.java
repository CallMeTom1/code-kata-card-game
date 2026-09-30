package com.pokemonarena.deck;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.cards.CardNature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckListsTest {

    @ParameterizedTest
    @EnumSource(DeckId.class)
    void everyDeckHasExactlyTwentyCards(DeckId id) {
        assertEquals(DeckLists.DECK_SIZE, DeckLists.cards(id).size());
    }

    @ParameterizedTest
    @EnumSource(DeckId.class)
    void everyDeckUsesAtMostThreeCopiesOfTheSameCard(DeckId id) {
        Map<String, Integer> copies = new HashMap<>();
        for (Card card : DeckLists.cards(id)) {
            copies.merge(card.id(), 1, Integer::sum);
        }
        copies.forEach((cardId, count) ->
                assertTrue(count <= DeckLists.MAX_COPIES, cardId + " appears " + count + " times in " + id));
    }

    @ParameterizedTest
    @EnumSource(DeckId.class)
    void everyDeckOnlyUsesCardsOfTheCatalog(DeckId id) {
        for (Card card : DeckLists.cards(id)) {
            assertSame(card, CardCatalog.byId(card.id()));
        }
    }

    @ParameterizedTest
    @EnumSource(DeckId.class)
    void everyDeckContainsAtLeastOnePokemon(DeckId id) {
        assertTrue(DeckLists.cards(id).stream().anyMatch(card -> card.nature() == CardNature.POKEMON));
    }

    @ParameterizedTest
    @EnumSource(DeckId.class)
    void deckListsAreImmutableAndDeterministic(DeckId id) {
        List<Card> first = DeckLists.cards(id);
        assertEquals(first, DeckLists.cards(id));
        assertThrows(UnsupportedOperationException.class, () -> first.add(CardCatalog.PIKACHU));
    }

    @Test
    void theBalancedDeckCoversTheFourCategories() {
        List<Card> cards = DeckLists.cards(DeckId.BALANCED);
        for (CardCategory category : CardCategory.values()) {
            assertTrue(cards.stream().anyMatch(card -> card.category() == category),
                    "no " + category + " card in the Balanced deck");
        }
    }

    @Test
    void shufflingADeckWithTheSameSeedIsReproducible() {
        Deck first = Deck.shuffled(DeckLists.cards(DeckId.AGGRO), new Random(7));
        Deck second = Deck.shuffled(DeckLists.cards(DeckId.AGGRO), new Random(7));

        assertEquals(DeckLists.DECK_SIZE, first.size());
        for (int i = 0; i < DeckLists.DECK_SIZE; i++) {
            assertEquals(first.draw(), second.draw());
        }
    }
}
