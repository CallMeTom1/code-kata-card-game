package com.arena.engine.events;

/** A bot's reasoning and its words to the opponent, so logs and the replay can show two AIs talking. */
public record BotSpoke(String player, String thought, String message) implements GameEvent {
}
