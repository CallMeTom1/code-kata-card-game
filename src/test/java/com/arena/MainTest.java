package com.arena;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    void given_no_arguments_when_main_runs_then_it_prints_the_title() {
        // Given
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        Main.run(new String[0], new PrintStream(out));

        // Then
        assertThat(out.toString()).contains("Skirmish Arena");
    }
}
