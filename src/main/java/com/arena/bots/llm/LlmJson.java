package com.arena.bots.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Parses model answers; any malformed answer becomes an exception the bots turn into a fallback. */
final class LlmJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LlmJson() {
    }

    static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Answer is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }
}
