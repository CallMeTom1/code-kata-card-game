package com.pokemonarena.hero;

import java.util.Objects;

/**
 * Immutable definition of a Hero. The mutable match state (current HP, mana, hand, deck,
 * discard pile, board) is introduced with the game engine in a later pass.
 */
public record Hero(String id, String name, int startingHp, HeroPower heroPower) {

    /** Every Hero starts with 30 HP and healing can never go above it. */
    public static final int STARTING_HP = 30;

    public Hero {
        Objects.requireNonNull(heroPower, "a Hero must have a Hero Power: " + id);
        if (startingHp <= 0) {
            throw new IllegalArgumentException("starting HP must be positive: " + id);
        }
    }

    public Hero(String id, String name, HeroPower heroPower) {
        this(id, name, STARTING_HP, heroPower);
    }
}
