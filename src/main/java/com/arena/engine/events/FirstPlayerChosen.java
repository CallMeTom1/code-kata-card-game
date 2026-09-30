package com.arena.engine.events;

/** Result of the coin flip; the second player gets The Coin. */
public record FirstPlayerChosen(String first, String second) implements GameEvent {
}
