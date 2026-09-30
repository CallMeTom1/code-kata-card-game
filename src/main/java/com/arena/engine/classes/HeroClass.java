package com.arena.engine.classes;

import com.arena.engine.cards.Card;

import java.util.List;

/** A playable class: its hero power, its own cards and its preset 20-card deck. */
public record HeroClass(String name, HeroPower heroPower, List<Card> classCards, List<Card> presetDeck) {

    /** Copies the lists so a class definition can never be changed by accident. */
    public HeroClass {
        classCards = List.copyOf(classCards);
        presetDeck = List.copyOf(presetDeck);
    }
}
