package com.arena.engine.cards;

import com.arena.engine.effects.Effect;

/**
 * An immutable card definition. A new card only needs a name/cost/category/effect,
 * never a new Java class, which keeps adding cards Open/Closed.
 */
public record Card(String name, int cost, CardCategory category, Effect effect, boolean immediate) {

    /** Resource and draw cards apply during the play phase because they change what can still be played. */
    public static Card immediate(String name, int cost, CardCategory category, Effect effect) {
        return new Card(name, cost, category, effect, true);
    }

    /** Everything else is queued and applied during the resolve phase, in play order. */
    public static Card queued(String name, int cost, CardCategory category, Effect effect) {
        return new Card(name, cost, category, effect, false);
    }
}
