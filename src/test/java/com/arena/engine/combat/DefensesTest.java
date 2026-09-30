package com.arena.engine.combat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefensesTest {

    @Test
    void given_two_armors_when_summed_then_they_stack() {
        // Given
        Defenses defenses = new Defenses();

        // When
        defenses.addArmor(3, 2);
        defenses.addArmor(5, 1);

        // Then
        assertThat(defenses.armor()).isEqualTo(8);
    }

    @Test
    void given_armor_3_when_absorbing_5_then_3_is_absorbed_and_armor_is_gone() {
        // Given
        Defenses defenses = new Defenses();
        defenses.addArmor(3, 2);

        // When
        int absorbed = defenses.absorbWithArmor(5);

        // Then
        assertThat(absorbed).isEqualTo(3);
        assertThat(defenses.armor()).isZero();
    }

    @Test
    void given_two_armors_when_absorbing_then_the_one_expiring_first_is_used_first() {
        // Given
        Defenses defenses = new Defenses();
        defenses.addArmor(5, 3);
        defenses.addArmor(2, 1);

        // When
        defenses.absorbWithArmor(2);
        int expired = defenses.onOwnerTurnStart();

        // Then
        assertThat(expired).isZero();
        assertThat(defenses.armor()).isEqualTo(5);
    }

    @Test
    void given_armor_for_2_turns_when_owner_starts_2_turns_then_it_expires_on_the_second() {
        // Given
        Defenses defenses = new Defenses();
        defenses.addArmor(4, 2);

        // When
        int expiredFirst = defenses.onOwnerTurnStart();
        int armorAfterFirst = defenses.armor();
        int expiredSecond = defenses.onOwnerTurnStart();

        // Then
        assertThat(expiredFirst).isZero();
        assertThat(armorAfterFirst).isEqualTo(4);
        assertThat(expiredSecond).isEqualTo(4);
        assertThat(defenses.armor()).isZero();
    }

    @Test
    void given_parry_for_1_turn_when_owner_starts_next_turn_then_parry_is_gone() {
        // Given
        Defenses defenses = new Defenses();
        defenses.addParry(2, 1);

        // When
        int parryBefore = defenses.parry();
        defenses.onOwnerTurnStart();

        // Then
        assertThat(parryBefore).isEqualTo(2);
        assertThat(defenses.parry()).isZero();
    }

    @Test
    void given_evasion_when_consumed_twice_then_only_the_first_succeeds() {
        // Given
        Defenses defenses = new Defenses();
        defenses.grantEvasion(2);

        // When
        boolean first = defenses.consumeEvasion();
        boolean second = defenses.consumeEvasion();

        // Then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }
}
