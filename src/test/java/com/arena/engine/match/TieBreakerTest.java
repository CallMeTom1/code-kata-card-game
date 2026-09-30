package com.arena.engine.match;

import com.arena.engine.player.Champion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TieBreakerTest {

    private final TieBreaker tieBreaker = new TieBreaker();

    @Test
    void given_turn_50_ends_when_hp_differ_then_higher_hp_wins() {
        // Given
        Champion p1 = new Champion("Alice", null);
        Champion p2 = new Champion("Bob", null);
        p2.loseHp(5);

        // When
        String winner = tieBreaker.decide(p1, p2, 0, 0);

        // Then
        assertThat(winner).isEqualTo("Alice");
    }

    @Test
    void given_equal_hp_when_tie_break_then_more_damage_dealt_wins() {
        // Given
        Champion p1 = new Champion("Alice", null);
        Champion p2 = new Champion("Bob", null);

        // When
        String winner = tieBreaker.decide(p1, p2, 10, 5);

        // Then
        assertThat(winner).isEqualTo("Alice");
    }

    @Test
    void given_equal_hp_and_damage_when_tie_break_then_draw() {
        // Given
        Champion p1 = new Champion("Alice", null);
        Champion p2 = new Champion("Bob", null);

        // When
        String winner = tieBreaker.decide(p1, p2, 10, 10);

        // Then
        assertThat(winner).isNull();
    }
}
