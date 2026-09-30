package com.arena.engine.events;

/** One hit on a champion: raw damage, what defenses prevented, and HP before and after. */
public record DamageDealt(String source, String target, int amount, int absorbed, int hpBefore, int hpAfter)
        implements GameEvent {
}
