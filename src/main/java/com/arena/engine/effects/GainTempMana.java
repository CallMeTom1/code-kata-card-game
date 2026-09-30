package com.arena.engine.effects;

import com.arena.engine.events.ManaGained;

/** Mana for this turn only (The Coin, Preparation), capped at 10. */
public record GainTempMana(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().gainMana(amount);
        ctx.events().publish(new ManaGained(ctx.caster().name(), ctx.source(), ctx.caster().mana(),
                ctx.caster().maxMana()));
    }
}
