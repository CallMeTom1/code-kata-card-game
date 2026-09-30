package com.arena.testing;

import com.arena.engine.combat.DamageResolver;
import com.arena.engine.combat.SimpleDamageResolver;
import com.arena.engine.effects.EffectContext;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** Minimal context so effects can be tested without running a whole match. */
public record TestEffectContext(Champion caster, Champion opponent, DamageResolver damage, EventPublisher events)
        implements EffectContext {

    /** Wires a simple resolver on the given publisher, which is what most effect tests need. */
    public static TestEffectContext between(Champion caster, Champion opponent, EventPublisher events) {
        return new TestEffectContext(caster, opponent, new SimpleDamageResolver(events), events);
    }
}
