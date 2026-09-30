package com.arena.engine.effects;

/** Eviscerate: goes through armor, and hits harder with Combo. */
public record DealDamageIgnoringArmor(int amount, int comboAmount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        int base = ctx.comboActive() ? comboAmount : amount;
        ctx.damage().dealIgnoringArmor(ctx.source(), ctx.opponent(), base + ctx.attackBonus());
    }
}
