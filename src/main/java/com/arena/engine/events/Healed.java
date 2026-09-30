package com.arena.engine.events;

/** HP restored; amount is what was actually healed (capped at 30 HP). */
public record Healed(String player, String source, int amount, int hpBefore, int hpAfter) implements GameEvent {
}
