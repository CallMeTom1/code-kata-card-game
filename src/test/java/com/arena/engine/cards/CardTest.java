package com.arena.engine.cards;

import com.arena.engine.effects.Effect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTest {

    @Test
    void given_a_negative_cost_when_creating_a_card_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> new Card("Broken", -1, CardCategory.ATTACK, Effect.NONE, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_a_blank_name_when_creating_a_card_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> new Card(" ", 1, CardCategory.ATTACK, Effect.NONE, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
