package com.arena.engine.events;

/** Poison damage at the start of the victim's turn; it ignores armor. */
public record PoisonTicked(String player, int amount, int hpBefore, int hpAfter) implements GameEvent {
}
