package com.arena.engine.classes;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.decks.DeckValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StandardClassesTest {

    private final HeroClasses classes = StandardClasses.all();

    @Test
    void given_the_standard_classes_when_listed_then_there_are_the_5_classes_of_the_design() {
        // Given / When / Then
        assertThat(classes.all()).extracting(HeroClass::name)
                .containsExactly("Mage", "Tank", "Swordsman", "Assassin", "Cleric");
    }

    @Test
    void given_all_cards_when_counted_then_there_are_39_distinct_cards_in_4_categories() {
        // Given
        List<Card> all = Stream.concat(NeutralCards.all().stream(),
                classes.all().stream().flatMap(c -> c.classCards().stream())).toList();

        // When
        Set<String> names = new HashSet<>(all.stream().map(Card::name).toList());
        Set<CardCategory> categories = new HashSet<>(all.stream().map(Card::category).toList());

        // Then
        assertThat(names).hasSize(39);
        assertThat(categories).containsExactlyInAnyOrder(CardCategory.values());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Mage", "Tank", "Swordsman", "Assassin", "Cleric"})
    void given_a_class_when_its_preset_deck_is_validated_then_it_is_legal(String className) {
        // Given
        HeroClass heroClass = classes.byName(className).orElseThrow();

        // When
        List<String> errors = new DeckValidator(NeutralCards.all()).validate(heroClass, heroClass.presetDeck());

        // Then
        assertThat(errors).isEmpty();
    }

    @Test
    void given_every_hero_power_when_checked_then_it_costs_2() {
        // Given / When / Then
        assertThat(classes.all()).allSatisfy(c -> assertThat(c.heroPower().cost()).isEqualTo(2));
    }

    @Test
    void given_the_card_sheet_when_reading_key_cards_then_numbers_match_design() {
        // Given
        HeroClass mage = classes.byName("Mage").orElseThrow();
        Card fireball = mage.classCards().stream().filter(c -> c.name().equals("Fireball")).findFirst().orElseThrow();

        // When / Then
        assertThat(fireball.cost()).isEqualTo(4);
        assertThat(fireball.category()).isEqualTo(CardCategory.ATTACK);
        assertThat(fireball.traits().damage()).isEqualTo(6);
        assertThat(NeutralCards.MANA_CRYSTAL.immediate()).isTrue();
        assertThat(NeutralCards.THE_COIN.cost()).isZero();
        assertThat(NeutralCards.all()).doesNotContain(NeutralCards.THE_COIN);
    }

    @Test
    void given_every_card_and_the_coin_when_read_then_each_has_a_rules_text_for_the_web_replay() {
        // Given
        List<Card> all = Stream.concat(Stream.concat(NeutralCards.all().stream(), Stream.of(NeutralCards.THE_COIN)),
                classes.all().stream().flatMap(c -> c.classCards().stream())).toList();

        // When / Then
        assertThat(all).allSatisfy(card -> assertThat(card.text()).as(card.name()).isNotBlank());
    }

    @Test
    void given_fireball_when_reading_its_text_then_it_describes_the_design_effect() {
        // Given
        HeroClass mage = classes.byName("Mage").orElseThrow();

        // When
        Card fireball = mage.classCards().stream().filter(c -> c.name().equals("Fireball")).findFirst().orElseThrow();

        // Then
        assertThat(fireball.text()).isEqualTo("Deal 6 damage.");
    }
}
