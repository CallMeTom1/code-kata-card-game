package com.arena.engine.effects;

/** Several separate hits: strong against armor, weak against Parry. The bonus is added to each hit. */
public record MultiHit(int hits, int amountPerHit) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        for (int i = 0; i < hits && !ctx.opponent().isDead(); i++) {
            ctx.damage().deal(ctx.source(), ctx.opponent(), amountPerHit + ctx.attackBonus());
        }
    }
}
