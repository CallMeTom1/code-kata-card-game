package com.arena.engine.cards;

/** The extra card the second player starts with: 0 mana, +1 mana this turn only, not part of any deck. */
public final class TheCoin {

    public static final String NAME = "The Coin";

    private TheCoin() {
    }

    public static Card card() {
        return Card.immediate(NAME, 0, CardCategory.RESOURCE, ctx -> ctx.caster().addTemporaryMana(1));
    }
}
