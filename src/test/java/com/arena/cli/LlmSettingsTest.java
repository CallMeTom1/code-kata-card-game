package com.arena.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmSettingsTest {

    @TempDir
    Path dir;

    @Test
    void given_only_the_committed_placeholder_when_reading_the_key_then_the_error_says_to_fill_env_local()
            throws IOException {
        // Given
        Files.writeString(dir.resolve(".env"), "ANTHROPIC_API_KEY=your-api-key-here\n");
        LlmSettings settings = LlmSettings.from(EnvFile.load(dir, Map.of()), null);

        // When / Then
        assertThatThrownBy(settings::apiKey).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".env.local");
    }

    @Test
    void given_a_model_on_the_command_line_and_in_env_when_read_then_the_command_line_wins() throws IOException {
        // Given
        Files.writeString(dir.resolve(".env"), "ARENA_LLM_MODEL=claude-haiku-4-5\n");

        // When
        LlmSettings settings = LlmSettings.from(EnvFile.load(dir, Map.of()), "claude-sonnet-5-5");

        // Then
        assertThat(settings.model()).isEqualTo("claude-sonnet-5-5");
    }

    @Test
    void given_no_model_anywhere_when_read_then_the_default_is_claude_opus_5_5() {
        // Given / When
        LlmSettings settings = LlmSettings.from(EnvFile.load(dir, Map.of()), null);

        // Then
        assertThat(settings.model()).isEqualTo("claude-opus-5-5");
    }

    @Test
    void given_a_real_key_in_env_local_when_checked_then_the_llm_bot_is_ready() throws IOException {
        // Given
        Files.writeString(dir.resolve(".env.local"), "ANTHROPIC_API_KEY=sk-ant-test\n");

        // When
        LlmSettings settings = LlmSettings.from(EnvFile.load(dir, Map.of()), null);

        // Then
        assertThat(settings.hasApiKey()).isTrue();
    }
}
