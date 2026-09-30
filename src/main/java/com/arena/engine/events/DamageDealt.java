package com.arena.engine.events;

/** One hit on a champion: raw damage, what defenses prevented, HP before and after, and armor left. */
public record DamageDealt(String source, String target, int amount, int absorbed, int hpBefore, int hpAfter,
                          int armorAfter) implements GameEvent {
}
