package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.effects.Effect;

/** Throw-away cards for tests, so engine tests do not depend on the real card list. */
public final class TestCards {

    private TestCards() {
    }

    /** A card that does nothing, for tests that only care about cost or hand size. */
    public static Card aCard(String name, int cost) {
        return new Card(name, cost, CardCategory.UTILITY, Effect.NONE, false);
    }

    /** A card that deals fixed damage to the opponent through the damage resolver. */
    public static Card aDamageCard(String name, int cost, int damage) {
        return new Card(name, cost, CardCategory.ATTACK,
                ctx -> ctx.damage().deal(name, ctx.opponent(), damage), false);
    }
}
