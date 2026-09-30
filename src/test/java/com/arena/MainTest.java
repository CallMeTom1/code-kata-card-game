package com.arena;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(args, new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void given_3_matches_when_main_runs_then_it_prints_the_aggregate_stats_only() {
        // Given / When
        int code = run("--matches", "3", "--seed", "1");

        // Then
        assertThat(code).isZero();
        assertThat(output()).contains("[STATS  ] P1 win rate").contains("Avg match length")
                .contains("Avg damage / match").doesNotContain("[MATCH  ]");
    }

    @Test
    void given_log_and_3_matches_when_main_runs_then_only_the_first_match_is_logged_in_full() {
        // Given / When
        run("--matches", "3", "--log");

        // Then
        assertThat(output().lines().filter(l -> l.startsWith("[MATCH  ]")).count()).isEqualTo(1);
        assertThat(output()).contains("[SETUP  ]").contains("[T01]").contains("[RESULT ]").contains("[STATS  ]");
    }

    @Test
    void given_the_same_arguments_when_main_runs_twice_then_the_output_is_identical() {
        // Given
        run("--matches", "20", "--p1", "Random:auto", "--p2", "Random:auto", "--log");
        String first = output();
        out.reset();

        // When
        run("--matches", "20", "--p1", "Random:auto", "--p2", "Random:auto", "--log");

        // Then
        assertThat(output()).isEqualTo(first);
    }

    @Test
    void given_a_wrong_argument_when_main_runs_then_it_prints_the_error_and_usage_and_returns_2() {
        // Given / When
        int code = run("--p1", "Genius:Mage");

        // Then
        assertThat(code).isEqualTo(2);
        assertThat(output()).contains("Genius").contains("Usage");
    }

    @Test
    void given_help_when_main_runs_then_it_prints_the_usage() {
        // Given / When
        int code = run("--help");

        // Then
        assertThat(code).isZero();
        assertThat(output()).contains("Usage").contains("--matches");
    }
}
