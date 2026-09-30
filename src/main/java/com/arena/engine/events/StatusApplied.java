package com.arena.engine.events;

/** A keyword was applied (FROZEN, POISON, EVASION, PARRY, NEXT ATTACK +X). */
public record StatusApplied(String target, String source, String status) implements GameEvent {
}
