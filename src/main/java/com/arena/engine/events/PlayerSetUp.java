package com.arena.engine.events;

import java.util.List;

/** What a side brought to the match: class, hero power and deck, shown before turn 1. */
public record PlayerSetUp(String player, String bot, String heroClass, String classChoice, String deckSource, String heroPower, int heroPowerCost, String heroPowerText, List<DeckEntry> deck) implements GameEvent {
}
