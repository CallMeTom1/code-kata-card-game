package com.arena.engine.events;

/** A minion attacks a Taunt minion or the enemy champion. */
public record MinionAttacked(String owner, String minion, int attack, int health, String target) implements GameEvent {
}
