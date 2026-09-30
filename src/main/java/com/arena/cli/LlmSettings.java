package com.arena.cli;

import java.util.Optional;

/** API key and model for the Llm bots; only resolved when an Llm bot actually plays. */
public final class LlmSettings {

    /** Default model of the Llm bots. */
    public static final String DEFAULT_MODEL = "claude-opus-5-5";
    private static final String KEY = "ANTHROPIC_API_KEY";
    private static final String PLACEHOLDER = "your-api-key-here";

    private final Optional<String> apiKey;
    private final String model;

    private LlmSettings(Optional<String> apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    /** Command-line model first, then ARENA_LLM_MODEL, then the default. */
    public static LlmSettings from(EnvFile env, String cliModel) {
        String model = cliModel != null ? cliModel : env.get("ARENA_LLM_MODEL").orElse(DEFAULT_MODEL);
        return new LlmSettings(env.get(KEY).filter(key -> !key.equals(PLACEHOLDER)), model);
    }

    /** The key, or a message telling exactly where to put it (never in the committed .env). */
    public String apiKey() {
        return apiKey.orElseThrow(() -> new IllegalArgumentException("The Llm bot needs a Claude API key: put "
                + KEY + "=<your key> in .env.local (git-ignored) or export " + KEY + "."));
    }

    /** Model id sent to the API. */
    public String model() {
        return model;
    }
}
