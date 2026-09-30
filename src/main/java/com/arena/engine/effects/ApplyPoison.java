package com.arena.engine.effects;

import com.arena.engine.events.StatusApplied;

/** Poisons the enemy champion: it loses HP at the start of its next turns, through armor. */
public record ApplyPoison(int amount, int turns) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.opponent().applyPoison(amount, turns);
        ctx.events().publish(new StatusApplied(ctx.opponent().name(), ctx.source(),
                "POISON " + amount + " (" + turns + " turns)"));
    }
}
