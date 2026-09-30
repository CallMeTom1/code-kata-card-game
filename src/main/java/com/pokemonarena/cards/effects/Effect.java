package com.pokemonarena.cards.effects;

/**
 * A single, reusable game effect. The same mechanism is used for Pokémon "when played"
 * abilities, Item cards and Hero Powers.
 */
public interface Effect {

    void apply(EffectContext context);

    /** Short human-readable description, used by the match log and by the card text. */
    String describe();
}
