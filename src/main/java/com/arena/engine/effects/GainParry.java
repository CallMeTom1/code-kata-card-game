package com.arena.engine.effects;

import com.arena.engine.events.StatusApplied;

/** Reduces each incoming hit for a number of the caster turns. */
public record GainParry(int amount, int turns) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().defenses().addParry(amount, turns);
        ctx.events().publish(new StatusApplied(ctx.caster().name(), ctx.source(),
                "PARRY " + amount + " (" + turns + " turn" + (turns > 1 ? "s" : "") + ")"));
    }
}
