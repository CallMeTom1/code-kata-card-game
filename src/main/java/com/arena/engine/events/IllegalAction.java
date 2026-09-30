package com.arena.engine.events;

/** A bot asked for something the rules forbid; its turn ends. */
public record IllegalAction(String player, String reason) implements GameEvent {
}
