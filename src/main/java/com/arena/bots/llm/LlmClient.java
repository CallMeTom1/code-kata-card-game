package com.arena.bots.llm;

/**
 * One question to a language model, answered as JSON. An interface so the bots never depend on a
 * real API (Dependency Inversion): tests use a scripted fake, Main wires the Claude client.
 */
public interface LlmClient {

    /** Returns the model's answer as JSON text matching {@code jsonSchema}; throws on API errors. */
    String complete(String system, String prompt, String jsonSchema);
}
