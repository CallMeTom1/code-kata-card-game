package com.arena.engine.events;

/** Mana phase result; frozen means one mana was lost to Freeze. */
public record ManaRefilled(String player, int mana, int maxMana, boolean frozen) implements GameEvent {
}
