package com.arena.engine.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionTest {

    @Test
    void given_new_champion_when_created_then_it_has_30_hp() {
        // Given / When
        Champion champion = new Champion("Alice", null);

        // Then
        assertThat(champion.hp()).isEqualTo(30);
    }

    @Test
    void given_two_armor_charges_when_stacked_then_total_armor_is_their_sum() {
        // Given
        Champion champion = new Champion("Alice", null);

        // When
        champion.addArmor(3, 2);
        champion.addArmor(7, 2);

        // Then
        assertThat(champion.totalArmor()).isEqualTo(10);
    }

    @Test
    void given_armor_with_two_turns_left_when_owner_starts_second_next_turn_then_armor_expires() {
        // Given
        Champion champion = new Champion("Alice", null);
        champion.addArmor(3, 2);

        // When
        champion.tickArmorForNewTurn();
        var expired = champion.tickArmorForNewTurn();

        // Then
        assertThat(expired).extracting(Armor::amount).containsExactly(3);
        assertThat(champion.totalArmor()).isZero();
    }

    @Test
    void given_turn_3_when_mana_grows_and_refills_then_max_mana_is_3() {
        // Given
        Champion champion = new Champion("Alice", null);

        // When
        champion.growAndRefillMana();
        champion.growAndRefillMana();
        champion.growAndRefillMana();

        // Then
        assertThat(champion.maxMana()).isEqualTo(3);
        assertThat(champion.mana()).isEqualTo(3);
    }

    @Test
    void given_max_mana_at_cap_when_mana_grows_again_then_it_stays_at_10() {
        // Given
        Champion champion = new Champion("Alice", null);
        for (int i = 0; i < 12; i++) {
            champion.growAndRefillMana();
        }

        // When / Then
        assertThat(champion.maxMana()).isEqualTo(10);
    }

    @Test
    void given_hand_of_10_when_a_card_is_added_then_it_is_burned() {
        // Given
        Champion champion = new Champion("Alice", null);
        var card = com.arena.engine.cards.NeutralCards.quickJab();
        for (int i = 0; i < 10; i++) {
            champion.addToHand(card);
        }

        // When
        boolean burned = champion.addToHand(card);

        // Then
        assertThat(burned).isTrue();
        assertThat(champion.hand()).hasSize(10);
    }
}
