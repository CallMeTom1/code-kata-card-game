package com.arena.engine.classes;

import com.arena.engine.cards.Card;

import java.util.List;

/** A playable class: its hero power and its preset 20-card deck. */
public record HeroClass(String name, HeroPower heroPower, List<Card> presetDeck) {

    /** Copies the deck so a class definition can never be changed by accident. */
    public HeroClass {
        presetDeck = List.copyOf(presetDeck);
    }
}
