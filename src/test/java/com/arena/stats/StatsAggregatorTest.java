package com.arena.stats;

import com.arena.engine.match.MatchResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StatsAggregatorTest {

    private static MatchResult won(String winner, String first, int rounds, int damage1, int damage2) {
        return new MatchResult("P1", "P2", first, winner, winner == null ? "turn limit, full tie" : "HP 0", rounds,
                10, 0, damage1, damage2);
    }

    @Test
    void given_3_matches_with_2_won_by_p1_when_aggregated_then_p1_win_rate_is_66_7_percent() {
        // Given
        StatsAggregator stats = new StatsAggregator();

        // When
        stats.add(won("P1", "P1", 10, 30, 20));
        stats.add(won("P1", "P2", 12, 30, 25));
        stats.add(won("P2", "P2", 14, 18, 30));
        AggregateStats result = stats.result();

        // Then
        assertThat(result.matches()).isEqualTo(3);
        assertThat(result.winRate1()).isCloseTo(66.67, within(0.01));
        assertThat(result.winRate2()).isCloseTo(33.33, within(0.01));
        assertThat(result.drawRate()).isZero();
        assertThat(result.firstPlayerWinRate()).isCloseTo(66.67, within(0.01));
        assertThat(result.averageRounds()).isEqualTo(12.0);
        assertThat(result.averageDamage1()).isEqualTo(26.0);
        assertThat(result.averageDamage2()).isEqualTo(25.0);
    }

    @Test
    void given_a_draw_when_aggregated_then_it_counts_as_a_draw_and_its_reason_is_kept() {
        // Given
        StatsAggregator stats = new StatsAggregator();

        // When
        stats.add(won(null, "P1", 50, 20, 20));
        AggregateStats result = stats.result();

        // Then
        assertThat(result.drawRate()).isEqualTo(100.0);
        assertThat(result.endReasons()).containsEntry("turn limit, full tie", 1);
    }
}
