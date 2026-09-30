package com.arena.engine.events;

/**
 * Marker for anything that happened in a match; every state change publishes one.
 * Not sealed on purpose: new event types can be added by any feature without touching this file.
 */
public interface GameEvent {
}
