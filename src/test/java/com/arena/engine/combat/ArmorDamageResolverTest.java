package com.arena.engine.combat;

import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorDamageResolverTest {

    @Test
    void given_armor_3_when_4_damage_dealt_then_hp_loses_1_and_armor_is_gone() {
        // Given
        Champion target = new Champion("Bob", null);
        target.addArmor(3, 2);
        DamageResolver resolver = new ArmorDamageResolver(new EventPublisher());

        // When
        resolver.deal("Alice", target, 4);

        // Then
        assertThat(target.hp()).isEqualTo(29);
        assertThat(target.totalArmor()).isZero();
    }

    @Test
    void given_parry_2_when_hit_of_3_then_1_damage_passes_after_armor() {
        // Given
        Champion target = new Champion("Bob", null);
        target.addParry(2, 1);
        DamageResolver resolver = new ArmorDamageResolver(new EventPublisher());

        // When
        resolver.deal("Alice", target, 3);

        // Then
        assertThat(target.hp()).isEqualTo(29);
    }

    @Test
    void given_damage_ignoring_armor_when_dealt_then_hp_is_hit_directly() {
        // Given
        Champion target = new Champion("Bob", null);
        target.addArmor(10, 2);
        DamageResolver resolver = new ArmorDamageResolver(new EventPublisher());

        // When
        resolver.dealIgnoringArmor("Poison", target, 3);

        // Then
        assertThat(target.hp()).isEqualTo(27);
        assertThat(target.totalArmor()).isEqualTo(10);
    }
}
