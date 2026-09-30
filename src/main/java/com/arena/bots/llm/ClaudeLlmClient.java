package com.arena.bots.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The real {@link LlmClient}: one Messages API call with a JSON schema (structured outputs), so the
 * answer always parses. The rules prompt is cached because it is the same for every call of a match.
 */
public final class ClaudeLlmClient implements LlmClient {

    private static final long MAX_TOKENS = 16_000L;
    /** Models that accept the server-side refusal fallback ("default" form); others would answer 400. */
    private static final Set<String> FALLBACK_MODELS = Set.of("claude-fable-5-1", "claude-opus-5-5", "claude-opus-5",
            "claude-sonnet-5-5");

    private final AnthropicClient client;
    private final String model;

    /** Builds the SDK client with an explicit key, so the key can come from .env.local. */
    public ClaudeLlmClient(String apiKey, String model) {
        this(AnthropicOkHttpClient.builder().apiKey(apiKey).build(), model);
    }

    ClaudeLlmClient(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    @Override
    public String complete(String system, String prompt, String jsonSchema) {
        Message message = client.messages().create(params(system, prompt, jsonSchema));
        String reason = message.stopReason().map(Object::toString).orElse("");
        if (!"end_turn".equals(reason)) {
            throw new IllegalStateException("model stopped with '" + reason + "'");
        }
        return message.content().stream().flatMap(block -> block.text().stream())
                .map(text -> text.text()).collect(Collectors.joining());
    }

    /** The request, separated so a test can check it without calling the API. */
    MessageCreateParams params(String system, String prompt, String jsonSchema) {
        OutputConfig.Builder output = OutputConfig.builder()
                .format(JsonOutputFormat.builder().schema(schema(jsonSchema)).build());
        if (supportsEffort()) {
            output.effort(OutputConfig.Effort.LOW);
        }
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(MAX_TOKENS)
                .systemOfTextBlockParams(List.of(TextBlockParam.builder().text(system)
                        .cacheControl(CacheControlEphemeral.builder().build()).build()))
                .outputConfig(output.build())
                .addUserMessage(prompt);
        if (FALLBACK_MODELS.contains(model)) {
            // server-side refusal fallback: a refused request is retried on another model
            builder.putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                    .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }
        return builder.build();
    }

    /** Haiku 4.5 rejects the effort setting (400); every newer model accepts it. */
    private boolean supportsEffort() {
        return !model.startsWith("claude-haiku");
    }

    private static JsonOutputFormat.Schema schema(String jsonSchema) {
        JsonNode root = LlmJson.parse(jsonSchema);
        Map<String, JsonValue> properties = new LinkedHashMap<>();
        root.fields().forEachRemaining(field -> properties.put(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
        return JsonOutputFormat.Schema.builder().additionalProperties(properties).build();
    }
}
