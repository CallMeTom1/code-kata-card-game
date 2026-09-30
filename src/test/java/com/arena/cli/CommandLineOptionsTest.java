package com.arena.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandLineOptionsTest {

    @Test
    void given_no_arguments_when_parsed_then_defaults_are_used() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[0]);

        // Then
        assertThat(options.matches()).isEqualTo(100);
        assertThat(options.player1()).isEqualTo(new PlayerSpec("Aggressive", "Mage"));
        assertThat(options.player2()).isEqualTo(new PlayerSpec("Defensive", "Tank"));
        assertThat(options.seed()).isEqualTo(42L);
        assertThat(options.log()).isFalse();
        assertThat(options.presetDecks()).isFalse();
        assertThat(options.names()).containsExactly("P1", "P2");
    }

    @Test
    void given_all_options_when_parsed_then_each_value_is_read() {
        // Given
        String[] args = {"--matches", "1000", "--p1", "Random:auto", "--p2", "Aggressive:Cleric", "--seed", "7",
                "--log", "--preset-decks", "--names", "Alice,Bob", "--json", "match.jsonl", "--stats-json", "stats.json"};

        // When
        CommandLineOptions options = CommandLineOptions.parse(args);

        // Then
        assertThat(options.matches()).isEqualTo(1000);
        assertThat(options.player1()).isEqualTo(new PlayerSpec("Random", "auto"));
        assertThat(options.player2()).isEqualTo(new PlayerSpec("Aggressive", "Cleric"));
        assertThat(options.seed()).isEqualTo(7L);
        assertThat(options.log()).isTrue();
        assertThat(options.presetDecks()).isTrue();
        assertThat(options.names()).containsExactly("Alice", "Bob");
        assertThat(options.jsonFile()).hasToString("match.jsonl");
        assertThat(options.statsJsonFile()).hasToString("stats.json");
    }

    @Test
    void given_a_bot_without_class_when_parsed_then_the_class_is_auto() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[] {"--p1", "Defensive"});

        // Then
        assertThat(options.player1()).isEqualTo(new PlayerSpec("Defensive", "auto"));
    }

    @Test
    void given_an_unknown_option_when_parsed_then_it_is_rejected_with_its_name() {
        // Given / When / Then
        assertThatThrownBy(() -> CommandLineOptions.parse(new String[] {"--turbo"}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("--turbo");
    }

    @Test
    void given_zero_matches_when_parsed_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> CommandLineOptions.parse(new String[] {"--matches", "0"}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("--matches");
    }

    @Test
    void given_a_missing_value_when_parsed_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> CommandLineOptions.parse(new String[] {"--seed"}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("--seed");
    }

    @Test
    void given_an_llm_model_when_parsed_then_it_is_kept_for_the_llm_bots() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[] {"--llm-model", "claude-sonnet-5-5"});

        // Then
        assertThat(options.llmModel()).isEqualTo("claude-sonnet-5-5");
    }

    @Test
    void given_serve_without_port_when_parsed_then_the_server_uses_port_8080() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[] {"--serve", "--log"});

        // Then
        assertThat(options.servePort()).isEqualTo(8080);
        assertThat(options.log()).isTrue();
    }

    @Test
    void given_serve_with_a_port_when_parsed_then_that_port_is_used() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[] {"--serve", "9000"});

        // Then
        assertThat(options.servePort()).isEqualTo(9000);
    }

    @Test
    void given_no_serve_option_when_parsed_then_no_server_is_started() {
        // Given / When
        CommandLineOptions options = CommandLineOptions.parse(new String[0]);

        // Then
        assertThat(options.servePort()).isZero();
    }
}
