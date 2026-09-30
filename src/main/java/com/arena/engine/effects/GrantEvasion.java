package com.arena.engine.effects;

import com.arena.engine.events.StatusApplied;

/** Arms the Evasion secret on the caster. */
public record GrantEvasion(int turns) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        ctx.caster().defenses().grantEvasion(turns);
        ctx.events().publish(new StatusApplied(ctx.caster().name(), ctx.source(), "EVASION"));
    }
}
