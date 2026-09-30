package com.arena.engine.events;

/** Marks the start of a player's turn so the log can be read turn by turn. */
public record TurnStarted(int turn, String player) implements GameEvent {
}
