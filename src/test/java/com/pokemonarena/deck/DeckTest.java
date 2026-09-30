package com.pokemonarena.deck;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckTest {

    @Test
    void drawsInOrderAndReturnsEmptyWhenExhausted() {
        Deck deck = new Deck(List.of(CardCatalog.PIKACHU, CardCatalog.POTION));

        assertEquals(Optional.of(CardCatalog.PIKACHU), deck.draw());
        assertEquals(Optional.of(CardCatalog.POTION), deck.draw());
        assertTrue(deck.isEmpty());
        assertEquals(Optional.empty(), deck.draw());
    }

    @Test
    void sameSeedProducesTheSameShuffle() {
        List<Card> cards = List.copyOf(CardCatalog.all());

        Deck first = Deck.shuffled(cards, new Random(42));
        Deck second = Deck.shuffled(cards, new Random(42));

        for (int i = 0; i < cards.size(); i++) {
            assertEquals(first.draw(), second.draw());
        }
    }
}
