package com.arena.engine.effects;

/** Smallest reusable building block of a card or hero power (deal damage, heal, draw…). */
@FunctionalInterface
public interface Effect {

    /** An effect that does nothing, so placeholder cards never need a null check. */
    Effect NONE = ctx -> {
    };

    /** Applies the effect; everything it may touch comes from the context, never from globals. */
    void apply(EffectContext ctx);

    /** Chains effects so a card like "heal 8 and draw 1" is a composition, not a new class. */
    default Effect andThen(Effect next) {
        return ctx -> {
            apply(ctx);
            next.apply(ctx);
        };
    }
}
