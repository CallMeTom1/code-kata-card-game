package com.arena.engine.effects;

/** Shield Slam: counts the armor the caster has when it resolves. */
public record DamageEqualToArmor() implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        int armor = ctx.caster().defenses().armor();
        if (armor > 0) {
            ctx.damage().deal(ctx.source(), ctx.opponent(), armor + ctx.attackBonus());
        }
    }
}
