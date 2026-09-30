package com.arena.engine.effects;

import com.arena.engine.events.StatusApplied;

/** The opponent gets 1 mana less on its next turn. */
public record Freeze() implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.opponent().freeze();
        ctx.events().publish(new StatusApplied(ctx.opponent().name(), ctx.source(), "FROZEN (-1 mana next turn)"));
    }
}
