package com.arena.testing;

import com.arena.bots.llm.LlmClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Test fake that answers with scripted JSON (or fails), and keeps every prompt it received. */
public final class FakeLlmClient implements LlmClient {

    private final Deque<String> answers = new ArrayDeque<>();
    private final List<String> prompts = new ArrayList<>();
    private RuntimeException failure;

    public FakeLlmClient answering(String... json) {
        answers.addAll(List.of(json));
        return this;
    }

    public FakeLlmClient failingWith(RuntimeException failure) {
        this.failure = failure;
        return this;
    }

    @Override
    public String complete(String system, String prompt, String jsonSchema) {
        prompts.add(prompt);
        if (failure != null) {
            throw failure;
        }
        if (answers.isEmpty()) {
            throw new IllegalStateException("FakeLlmClient: no scripted answer left");
        }
        return answers.poll();
    }

    public List<String> prompts() {
        return prompts;
    }
}
