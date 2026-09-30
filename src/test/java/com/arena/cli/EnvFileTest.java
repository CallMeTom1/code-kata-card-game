package com.arena.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvFileTest {

    @TempDir
    Path dir;

    @Test
    void given_a_key_only_in_dot_env_when_read_then_its_value_is_returned() throws IOException {
        // Given
        Files.writeString(dir.resolve(".env"), "# comment\nANTHROPIC_API_KEY=from-env-file\n");

        // When
        EnvFile env = EnvFile.load(dir, Map.of());

        // Then
        assertThat(env.get("ANTHROPIC_API_KEY")).contains("from-env-file");
    }

    @Test
    void given_a_key_in_dot_env_local_and_dot_env_when_read_then_the_local_file_wins() throws IOException {
        // Given
        Files.writeString(dir.resolve(".env"), "ANTHROPIC_API_KEY=placeholder\n");
        Files.writeString(dir.resolve(".env.local"), "ANTHROPIC_API_KEY=\"real-key\"\n");

        // When
        EnvFile env = EnvFile.load(dir, Map.of());

        // Then
        assertThat(env.get("ANTHROPIC_API_KEY")).contains("real-key");
    }

    @Test
    void given_a_key_in_the_environment_and_in_files_when_read_then_the_environment_wins() throws IOException {
        // Given
        Files.writeString(dir.resolve(".env.local"), "ANTHROPIC_API_KEY=real-key\n");

        // When
        EnvFile env = EnvFile.load(dir, Map.of("ANTHROPIC_API_KEY", "exported-key"));

        // Then
        assertThat(env.get("ANTHROPIC_API_KEY")).contains("exported-key");
    }

    @Test
    void given_no_file_and_no_variable_when_read_then_the_key_is_absent() {
        // Given / When
        EnvFile env = EnvFile.load(dir, Map.of());

        // Then
        assertThat(env.get("ANTHROPIC_API_KEY")).isEmpty();
    }
}
