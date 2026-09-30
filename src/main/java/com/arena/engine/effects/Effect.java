package com.arena.engine.effects;

/** A small, reusable building block; a card or hero power is a composition of these (Open/Closed). */
@FunctionalInterface
public interface Effect {

    /** Applies this effect's rule against the current match state exposed by the context. */
    void apply(EffectContext ctx);

    /** Chains another effect after this one, so cards can compose several building blocks in order. */
    default Effect andThen(Effect next) {
        return ctx -> {
            apply(ctx);
            next.apply(ctx);
        };
    }
}
