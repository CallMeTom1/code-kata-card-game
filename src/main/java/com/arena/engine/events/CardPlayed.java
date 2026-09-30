package com.arena.engine.events;

/** Published when a card leaves the hand and mana is spent, before its effect resolves. */
public record CardPlayed(String player, String card, int cost, int manaLeft) implements GameEvent {
}
