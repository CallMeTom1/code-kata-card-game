package com.arena.engine.events;

/** A minion arrived on its owner's board. */
public record MinionSummoned(String owner, String minion, int attack, int health, boolean taunt, int boardSize) implements GameEvent {
}
