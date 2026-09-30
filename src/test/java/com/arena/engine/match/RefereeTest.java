package com.arena.engine.match;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefereeTest {

    @Test
    void given_both_champions_at_0_hp_when_judged_then_the_match_is_a_draw() {
        // Given / When
        Verdict verdict = Referee.onDeath("P1", true, "P2", true);

        // Then
        assertThat(verdict.winner()).isNull();
        assertThat(verdict.reason()).isEqualTo("both at 0 HP");
    }

    @Test
    void given_only_p2_at_0_hp_when_judged_then_p1_wins() {
        // Given / When
        Verdict verdict = Referee.onDeath("P1", false, "P2", true);

        // Then
        assertThat(verdict.winner()).isEqualTo("P1");
        assertThat(verdict.reason()).isEqualTo("HP 0");
    }

    @Test
    void given_turn_50_ends_with_different_hp_when_tie_break_then_higher_hp_wins() {
        // Given / When
        Verdict verdict = Referee.tieBreak("P1", 12, 40, "P2", 9, 60);

        // Then
        assertThat(verdict.winner()).isEqualTo("P1");
        assertThat(verdict.reason()).isEqualTo("turn limit, more HP");
    }

    @Test
    void given_equal_hp_when_tie_break_then_more_damage_dealt_wins() {
        // Given / When
        Verdict verdict = Referee.tieBreak("P1", 10, 40, "P2", 10, 55);

        // Then
        assertThat(verdict.winner()).isEqualTo("P2");
        assertThat(verdict.reason()).isEqualTo("turn limit, more damage dealt");
    }

    @Test
    void given_equal_hp_and_damage_when_tie_break_then_draw() {
        // Given / When
        Verdict verdict = Referee.tieBreak("P1", 10, 40, "P2", 10, 40);

        // Then
        assertThat(verdict.winner()).isNull();
        assertThat(verdict.reason()).isEqualTo("turn limit, full tie");
    }
}
