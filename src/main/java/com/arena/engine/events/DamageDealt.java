package com.arena.engine.events;

/** Published every time a target loses HP, so stats can tell absorbed damage from HP actually lost. */
public record DamageDealt(String source, String target, int amount, int absorbed, int targetHpLeft) implements GameEvent {
}
