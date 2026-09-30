package com.arena.engine.effects;

import com.arena.engine.events.Healed;
import com.arena.engine.player.Champion;

/** Restores the caster HP, capped at 30. */
public record Heal(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        Champion caster = ctx.caster();
        int before = caster.hp();
        caster.heal(amount);
        ctx.events().publish(new Healed(caster.name(), ctx.source(), caster.hp() - before, before, caster.hp()));
    }
}
