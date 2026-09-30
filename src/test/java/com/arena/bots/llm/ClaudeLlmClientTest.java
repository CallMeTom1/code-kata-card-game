package com.arena.bots.llm;

import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaudeLlmClientTest {

    @Test
    void given_a_schema_when_the_request_is_built_then_it_asks_for_that_json_with_the_chosen_model() {
        // Given
        ClaudeLlmClient client = new ClaudeLlmClient("test-key", "claude-opus-5-5");

        // When
        MessageCreateParams params = client.params("rules", "your turn", LlmBot.TURN_SCHEMA);

        // Then
        assertThat(params.model().asString()).isEqualTo("claude-opus-5-5");
        assertThat(params.outputConfig().orElseThrow().format().orElseThrow().schema()._additionalProperties())
                .containsKeys("type", "required", "properties");
        assertThat(params.system().orElseThrow().toString()).contains("rules");
    }

    @Test
    void given_the_sdk_stop_reason_end_turn_when_printed_then_it_matches_the_value_the_client_expects() {
        // Given / When / Then
        assertThat(StopReason.END_TURN.toString()).isEqualTo("end_turn");
    }
}
