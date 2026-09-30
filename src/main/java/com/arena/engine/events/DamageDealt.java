package com.arena.engine.events;

/** Carries both the raw damage and what armor absorbed, so the log explains every HP change. */
public record DamageDealt(String source, String target, int amount, int absorbed, int targetHpLeft)
        implements GameEvent {
}
