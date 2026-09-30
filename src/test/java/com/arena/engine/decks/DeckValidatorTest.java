package com.arena.engine.decks;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.StandardClasses;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckValidatorTest {

    private final DeckValidator validator = new DeckValidator(NeutralCards.all());
    private final HeroClass mage = StandardClasses.all().byName("Mage").orElseThrow();
    private final HeroClass tank = StandardClasses.all().byName("Tank").orElseThrow();

    @Test
    void given_a_deck_of_19_cards_when_validated_then_it_is_rejected() {
        // Given
        List<Card> deck = new ArrayList<>(mage.presetDeck());
        deck.removeLast();

        // When / Then
        assertThat(validator.validate(mage, deck)).anyMatch(error -> error.contains("20 cards"));
    }

    @Test
    void given_a_deck_with_3_copies_when_validated_then_it_is_rejected() {
        // Given
        List<Card> deck = new ArrayList<>(mage.presetDeck());
        deck.set(deck.size() - 1, deck.getFirst());

        // When / Then
        assertThat(validator.validate(mage, deck)).anyMatch(error -> error.contains("copies"));
    }

    @Test
    void given_a_deck_with_5_class_cards_when_validated_then_it_is_rejected() {
        // Given
        List<Card> deck = new ArrayList<>();
        mage.classCards().stream().limit(5).forEach(deck::add);
        for (Card neutral : NeutralCards.all()) {
            deck.add(neutral);
            deck.add(neutral);
        }
        List<Card> twenty = deck.subList(0, 20);

        // When / Then
        assertThat(validator.validate(mage, twenty)).anyMatch(error -> error.contains("class cards"));
    }

    @Test
    void given_a_mage_deck_with_a_tank_card_when_validated_then_it_is_rejected() {
        // Given
        List<Card> deck = new ArrayList<>(mage.presetDeck());
        deck.set(0, tank.classCards().getFirst());

        // When / Then
        assertThat(validator.validate(mage, deck)).anyMatch(error -> error.contains("not allowed"));
    }
}
