package com.arena.engine.events;

/** Marks the start of a player's turn with a snapshot, so the log can be read turn by turn. */
public record TurnStarted(int round, String player, int hp, int armor, int handSize, int deckSize) implements GameEvent {
}
