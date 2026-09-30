package com.arena.engine.player;

import com.arena.engine.cards.Card;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.testing.ChampionBuilder.aChampion;
import static com.arena.testing.TestCards.aCard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChampionTest {

    @Test
    void given_a_new_champion_when_created_then_it_has_30_hp_and_no_mana() {
        // Given / When
        Champion champion = new Champion("P1", List.of());

        // Then
        assertThat(champion.hp()).isEqualTo(30);
        assertThat(champion.mana()).isZero();
        assertThat(champion.maxMana()).isZero();
        assertThat(champion.isDead()).isFalse();
    }

    @Test
    void given_a_deck_when_champion_is_created_then_the_deck_keeps_its_order() {
        // Given
        Card first = aCard("First", 1);
        Card second = aCard("Second", 2);

        // When
        Champion champion = new Champion("P1", List.of(first, second));

        // Then
        assertThat(champion.deck()).containsExactly(first, second);
    }

    @Test
    void given_30_hp_when_losing_4_hp_then_26_hp_remain() {
        // Given
        Champion champion = aChampion().build();

        // When
        champion.loseHp(4);

        // Then
        assertThat(champion.hp()).isEqualTo(26);
    }

    @Test
    void given_3_hp_when_losing_5_hp_then_hp_stops_at_0_and_champion_is_dead() {
        // Given
        Champion champion = aChampion().withHp(3).build();

        // When
        champion.loseHp(5);

        // Then
        assertThat(champion.hp()).isZero();
        assertThat(champion.isDead()).isTrue();
    }

    @Test
    void given_20_hp_when_healed_by_5_then_25_hp() {
        // Given
        Champion champion = aChampion().withHp(20).build();

        // When
        champion.heal(5);

        // Then
        assertThat(champion.hp()).isEqualTo(25);
    }

    @Test
    void given_28_hp_when_healed_by_5_then_hp_is_capped_at_30() {
        // Given
        Champion champion = aChampion().withHp(28).build();

        // When
        champion.heal(5);

        // Then
        assertThat(champion.hp()).isEqualTo(30);
    }

    @Test
    void given_3_mana_when_spending_2_then_1_mana_remains() {
        // Given
        Champion champion = aChampion().withMana(3).build();

        // When
        champion.spendMana(2);

        // Then
        assertThat(champion.mana()).isEqualTo(1);
    }

    @Test
    void given_1_mana_when_spending_2_then_it_is_refused() {
        // Given
        Champion champion = aChampion().withMana(1).build();

        // When / Then
        assertThatThrownBy(() -> champion.spendMana(2)).isInstanceOf(IllegalStateException.class);
        assertThat(champion.mana()).isEqualTo(1);
    }

    @Test
    void given_any_champion_when_mana_is_refilled_above_10_then_it_is_capped_at_10() {
        // Given
        Champion champion = aChampion().build();

        // When
        champion.refillMana(12);

        // Then
        assertThat(champion.maxMana()).isEqualTo(10);
        assertThat(champion.mana()).isEqualTo(10);
    }

    @Test
    void given_an_empty_hand_when_a_card_is_added_then_hand_contains_it() {
        // Given
        Champion champion = aChampion().build();
        Card card = aCard("Strike", 2);

        // When
        champion.addToHand(card);

        // Then
        assertThat(champion.hand()).containsExactly(card);
    }
}
