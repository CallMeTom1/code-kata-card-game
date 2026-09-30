package com.arena.engine.effects;

import com.arena.engine.events.StatusApplied;

/** "Next Attack +X": used by the next Attack card that deals damage, on each of its hits. */
public record BuffNextAttack(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().addAttackBonus(amount);
        ctx.events().publish(new StatusApplied(ctx.caster().name(), ctx.source(),
                "NEXT ATTACK +" + ctx.caster().attackBonus()));
    }
}
