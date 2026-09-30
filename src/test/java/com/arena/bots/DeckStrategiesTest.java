package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.classes.StandardClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.DeckValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class DeckStrategiesTest {

    private final HeroClasses classes = StandardClasses.all();
    private final DeckValidator validator = new DeckValidator(NeutralCards.all());

    @ParameterizedTest
    @ValueSource(strings = {"Mage", "Tank", "Swordsman", "Assassin", "Cleric"})
    void given_aggressive_strategy_when_building_any_class_deck_then_it_is_legal_and_has_no_pure_heal(String name) {
        // Given
        HeroClass heroClass = classes.byName(name).orElseThrow();

        // When
        List<Card> deck = new AggressiveDeckStrategy().buildDeck(heroClass, NeutralCards.all());

        // Then
        assertThat(validator.validate(heroClass, deck)).isEmpty();
        assertThat(deck).noneMatch(c -> c.traits().heal() > 0 && c.traits().damage() == 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mage", "Tank", "Swordsman", "Assassin", "Cleric"})
    void given_defensive_strategy_when_building_any_class_deck_then_it_is_legal_with_at_least_6_attacks(String name) {
        // Given
        HeroClass heroClass = classes.byName(name).orElseThrow();

        // When
        List<Card> deck = new DefensiveDeckStrategy().buildDeck(heroClass, NeutralCards.all());

        // Then
        assertThat(validator.validate(heroClass, deck)).isEmpty();
        assertThat(deck.stream().filter(c -> c.category() == CardCategory.ATTACK).count()).isGreaterThanOrEqualTo(6);
    }

    @Test
    void given_defensive_strategy_when_building_then_heals_and_armor_come_before_extra_attacks() {
        // Given
        HeroClass tank = classes.byName("Tank").orElseThrow();

        // When
        List<Card> deck = new DefensiveDeckStrategy().buildDeck(tank, NeutralCards.all());

        // Then
        assertThat(deck).extracting(Card::name).contains("Fortress", "Last Stand", "Iron Wall", "Healing Potion");
    }

    @Test
    void given_random_strategy_and_the_same_seed_when_building_twice_then_the_decks_are_identical_and_legal() {
        // Given
        HeroClass mage = classes.byName("Mage").orElseThrow();

        // When
        List<Card> first = new RandomDeckStrategy(new Random(3)).buildDeck(mage, NeutralCards.all());
        List<Card> second = new RandomDeckStrategy(new Random(3)).buildDeck(mage, NeutralCards.all());

        // Then
        assertThat(first).isEqualTo(second);
        assertThat(validator.validate(mage, first)).isEmpty();
    }

    @Test
    void given_auto_class_when_each_strategy_chooses_then_the_design_defaults_are_used() {
        // Given
        DeckStrategy aggressive = new AggressiveDeckStrategy();
        DeckStrategy defensive = new DefensiveDeckStrategy();

        // When / Then
        assertThat(aggressive.chooseClass(classes).name()).isEqualTo("Assassin");
        assertThat(defensive.chooseClass(classes).name()).isEqualTo("Tank");
        assertThat(new RandomDeckStrategy(new Random(1)).chooseClass(classes)).isIn(classes.all());
    }
}
