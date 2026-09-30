package com.arena.engine.player;

/** One stack of timed armor; several can coexist and are removed independently (armors stack). */
public record Armor(int amount, int turnsLeft) {

    /** A new armor charge with the given absorption and remaining owner turns before it expires. */
    public static Armor of(int amount, int turns) {
        return new Armor(amount, turns);
    }

    /** One less owner turn before this charge expires. */
    public Armor ticked() {
        return new Armor(amount, turnsLeft - 1);
    }

    /** True once this charge has no turns left and must be removed. */
    public boolean expired() {
        return turnsLeft <= 0;
    }
}
