package com.arena.engine.effects;

import com.arena.engine.combat.DamageResolver;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** What an {@link Effect} is allowed to see and touch, kept narrow so effects stay composable. */
public interface EffectContext {

    /** The champion who played the card or activated the hero power. */
    Champion caster();

    /** The other champion in the match. */
    Champion opponent();

    /** The single place that turns raw damage into HP loss, so armor/Parry stay centralized. */
    DamageResolver damage();

    /** Where an effect reports what it did, so logging/stats never need to know about effects. */
    EventPublisher events();

    /** True once another card has already been played by the caster this turn, for Combo effects. */
    boolean comboActive();
}
