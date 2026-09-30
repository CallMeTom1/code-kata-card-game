package com.arena.engine.events;

/** Shows which card a bot chose and what it cost, so bot decisions can be followed. */
public record CardPlayed(String player, String card, int cost, int manaLeft) implements GameEvent {
}
