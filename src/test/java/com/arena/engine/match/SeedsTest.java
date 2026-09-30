package com.arena.engine.match;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeedsTest {

    @Test
    void given_1000_consecutive_seeds_when_flipping_the_coin_then_each_player_starts_about_half_the_time() {
        // Given
        int player1First = 0;

        // When
        for (long seed = 42; seed < 1042; seed++) {
            if (CoinFlip.RANDOM.player1First(Seeds.random(seed))) {
                player1First++;
            }
        }

        // Then
        assertThat(player1First).isBetween(450, 550);
    }

    @Test
    void given_the_same_seed_when_creating_two_randoms_then_they_produce_the_same_numbers() {
        // Given / When / Then
        assertThat(Seeds.random(7).nextLong()).isEqualTo(Seeds.random(7).nextLong());
    }
}
