package com.arena.engine.effects;

import com.arena.engine.events.ManaGained;

/** Permanent empty mana crystals, usable from the next turn. */
public record GainMaxMana(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().gainMaxMana(amount);
        ctx.events().publish(new ManaGained(ctx.caster().name(), ctx.source(), ctx.caster().mana(),
                ctx.caster().maxMana()));
    }
}
