package com.pokemonarena.session;

import com.pokemonarena.deck.DeckId;
import com.pokemonarena.hero.Hero;

import java.util.Objects;

/** The Hero and Deck chosen for one side. Any Hero may be paired with any Deck. */
public record PlayerSetup(Hero hero, DeckId deck) {

    public PlayerSetup {
        Objects.requireNonNull(hero, "hero");
        Objects.requireNonNull(deck, "deck");
    }

    @Override
    public String toString() {
        return hero.name() + " + " + deck;
    }
}
