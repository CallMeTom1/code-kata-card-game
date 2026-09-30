package com.arena.engine.effects;

/** Deals damage to the enemy champion; the attack bonus is added. */
public record DealDamage(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.damage().deal(ctx.source(), ctx.opponent(), amount + ctx.attackBonus());
    }
}
