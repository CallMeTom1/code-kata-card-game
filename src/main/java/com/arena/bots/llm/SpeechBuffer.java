package com.arena.bots.llm;

import com.arena.engine.match.Speech;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Collects what an LLM player thought and said between two engine calls, so nothing is lost. */
final class SpeechBuffer {

    private final List<Speech> pending = new ArrayList<>();

    void add(String thought, String message) {
        pending.add(new Speech(thought, message));
    }

    /** Thoughts joined in order, and the last non-empty message: one event per engine call. */
    Optional<Speech> take() {
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        String thought = String.join(" ", pending.stream().map(Speech::thought).filter(t -> !t.isEmpty()).toList());
        String message = pending.stream().map(Speech::message).filter(m -> !m.isEmpty())
                .reduce((first, second) -> second).orElse("");
        pending.clear();
        return Optional.of(new Speech(thought, message));
    }
}
