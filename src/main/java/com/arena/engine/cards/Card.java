package com.arena.engine.cards;

import com.arena.engine.effects.Effect;

import java.util.Objects;

/**
 * A card is data plus a composition of reusable effects, so adding a card never needs new
 * engine code (Open/Closed).
 *
 * @param immediate true when the effect must apply during the play phase (mana, draw),
 *                  false when it waits for the resolve phase
 * @param traits    numbers the bots use to rank cards
 */
public record Card(String name, int cost, CardCategory category, Effect effect, boolean immediate,
                   CardTraits traits) {

    /** Rejects impossible cards early, so a typo in a card list fails at startup, not mid-match. */
    public Card {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A card needs a name");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Card cost cannot be negative: " + name);
        }
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(traits, "traits");
    }

    /** For tests and simple cards that bots do not need to rank. */
    public Card(String name, int cost, CardCategory category, Effect effect, boolean immediate) {
        this(name, cost, category, effect, immediate, CardTraits.NONE);
    }

    /** Human-readable text, useful when debugging a deck. */
    @Override
    public String toString() {
        return name + " (" + cost + ")";
    }
}
