package com.arena.engine.events;

/** Published at the start of every turn, before the draw phase of that player. */
public record TurnStarted(int turn, String player) implements GameEvent {
}
