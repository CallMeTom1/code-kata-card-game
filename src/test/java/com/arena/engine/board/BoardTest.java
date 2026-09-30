package com.arena.engine.board;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    private static final MinionTemplate WOLF = new MinionTemplate("Wolf", 2, 2, false);
    private static final MinionTemplate GOLEM = new MinionTemplate("Iron Golem", 2, 6, true);

    @Test
    void given_an_empty_board_when_a_minion_is_summoned_then_the_board_has_1_minion() {
        // Given
        Board board = new Board();

        // When
        board.summon(WOLF, 1);

        // Then
        assertThat(board.minions()).extracting(Minion::name).containsExactly("Wolf");
    }

    @Test
    void given_a_board_of_7_when_a_minion_is_summoned_then_the_summon_fizzles() {
        // Given
        Board board = new Board();
        for (int i = 0; i < 7; i++) {
            board.summon(WOLF, 1);
        }

        // When
        boolean summoned = board.summon(WOLF, 1).isPresent();

        // Then
        assertThat(summoned).isFalse();
        assertThat(board.minions()).hasSize(7);
    }

    @Test
    void given_a_minion_summoned_this_round_when_asked_if_it_can_attack_then_no_until_next_round() {
        // Given
        Board board = new Board();
        Minion wolf = board.summon(WOLF, 3).orElseThrow();

        // When / Then
        assertThat(wolf.canAttack(3)).isFalse();
        assertThat(wolf.canAttack(4)).isTrue();
    }

    @Test
    void given_a_0_attack_minion_when_asked_if_it_can_attack_then_never() {
        // Given
        Minion wall = new Board().summon(new MinionTemplate("Shieldbearer", 0, 3, true), 1).orElseThrow();

        // When / Then
        assertThat(wall.canAttack(5)).isFalse();
    }

    @Test
    void given_a_taunt_and_a_normal_minion_when_listing_taunts_then_only_the_taunt_is_returned() {
        // Given
        Board board = new Board();
        board.summon(WOLF, 1);
        board.summon(GOLEM, 1);

        // When / Then
        assertThat(board.firstTaunt()).map(Minion::name).contains("Iron Golem");
    }
}
