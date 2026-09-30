package com.arena.engine.classes;

import com.arena.engine.effects.Effect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeroClassesTest {

    private static final HeroClass MAGE =
            new HeroClass("Mage", new HeroPower("Fireblast", 2, Effect.NONE), List.of());

    @Test
    void given_a_registry_with_mage_when_looking_up_mage_in_any_case_then_it_is_found() {
        // Given
        HeroClasses classes = new HeroClasses(List.of(MAGE));

        // When / Then
        assertThat(classes.byName("mage")).contains(MAGE);
        assertThat(classes.byName("MAGE")).contains(MAGE);
    }

    @Test
    void given_a_registry_when_looking_up_an_unknown_class_then_nothing_is_found() {
        // Given
        HeroClasses classes = new HeroClasses(List.of(MAGE));

        // When / Then
        assertThat(classes.byName("Necromancer")).isEmpty();
    }

    @Test
    void given_a_negative_cost_when_creating_a_hero_power_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> new HeroPower("Broken", -1, Effect.NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
