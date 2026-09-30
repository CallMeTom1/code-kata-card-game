package com.arena.engine.events;

/** The Evasion secret prevented a hit. */
public record EvasionTriggered(String player, String source, int prevented) implements GameEvent {
}
