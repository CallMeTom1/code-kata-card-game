package com.arena.engine.classes;

import com.arena.engine.effects.Effect;

import java.util.Objects;

/** The class ability usable once per turn; built from the same effects as cards. */
public record HeroPower(String name, int cost, Effect effect) {

    /** Rejects impossible values early, like {@link com.arena.engine.cards.Card} does. */
    public HeroPower {
        if (cost < 0) {
            throw new IllegalArgumentException("Hero power cost cannot be negative: " + name);
        }
        Objects.requireNonNull(effect, "effect");
    }
}
