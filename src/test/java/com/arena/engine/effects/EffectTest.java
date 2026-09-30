package com.arena.engine.effects;

import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;
import com.arena.testing.TestEffectContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.arena.testing.ChampionBuilder.aChampion;
import static org.assertj.core.api.Assertions.assertThat;

class EffectTest {

    @Test
    void given_two_effects_composed_with_and_then_when_applied_then_both_run_in_order() {
        // Given
        List<String> calls = new ArrayList<>();
        Effect first = ctx -> calls.add("first");
        Effect second = ctx -> calls.add("second");
        Champion caster = aChampion().named("P1").build();
        Champion opponent = aChampion().named("P2").build();

        // When
        first.andThen(second).apply(TestEffectContext.between(caster, opponent, new EventPublisher()));

        // Then
        assertThat(calls).containsExactly("first", "second");
    }
}
