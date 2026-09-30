package com.arena.engine.effects;

/** Draws cards for the caster, with burn and fatigue rules. */
public record DrawCards(int count) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.draw(ctx.caster(), count);
    }
}
