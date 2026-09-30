package com.arena.stats;

import com.arena.engine.match.MatchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatsJsonWriterTest {

    @Test
    void given_two_matches_when_written_then_json_has_the_sides_the_summary_and_one_entry_per_match() {
        // Given
        StatsAggregator stats = new StatsAggregator();
        MatchResult first = new MatchResult("P1", "P2", "P1", "P1", "HP 0", 9, 12, 0, 30, 18);
        MatchResult second = new MatchResult("P1", "P2", "P2", null, "turn limit, full tie", 50, 5, 5, 25, 25);
        stats.add(first);
        stats.add(second);
        List<MatchRecord> records = List.of(new MatchRecord(42, "Mage", "Tank", first),
                new MatchRecord(43, "Mage", "Tank", second));

        // When
        String json = StatsJsonWriter.write("P1", "Aggressive:Mage", "P2", "Defensive:Tank", 42, stats.result(), records);

        // Then
        assertThat(json).startsWith("{\"player1\":\"P1\",\"label1\":\"Aggressive:Mage\",\"player2\":\"P2\"")
                .contains("\"summary\":{\"matches\":2,\"wins1\":1,\"wins2\":0,\"draws\":1")
                .contains("\"winRate1\":50.0")
                .contains("{\"seed\":42,\"class1\":\"Mage\",\"class2\":\"Tank\",\"first\":\"P1\",\"winner\":\"P1\","
                        + "\"reason\":\"HP 0\",\"rounds\":9,\"hp1\":12,\"hp2\":0,\"damage1\":30,\"damage2\":18}")
                .contains("\"winner\":null");
    }
}
