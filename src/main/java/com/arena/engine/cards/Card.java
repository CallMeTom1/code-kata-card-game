package com.arena.engine.cards;

import com.arena.engine.effects.Effect;

import java.util.Objects;

/**
 * A card is data plus a composition of reusable effects, so adding a card never needs new
 * engine code (Open/Closed).
 *
 * @param immediate true when the effect must apply during the play phase (mana, draw),
 *                  false when it waits for the resolve phase
 */
public record Card(String name, int cost, CardCategory category, Effect effect, boolean immediate) {

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
    }
}
