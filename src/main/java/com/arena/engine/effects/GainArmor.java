package com.arena.engine.effects;

import com.arena.engine.events.ArmorGained;

/** Adds an armor layer to the caster for a number of its turns. */
public record GainArmor(int amount, int turns) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().defenses().addArmor(amount, turns);
        ctx.events().publish(new ArmorGained(ctx.caster().name(), ctx.source(), amount, turns,
                ctx.caster().defenses().armor()));
    }
}
