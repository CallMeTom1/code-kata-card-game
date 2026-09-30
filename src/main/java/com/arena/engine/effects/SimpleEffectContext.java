package com.arena.engine.effects;

import com.arena.engine.combat.DamageResolver;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** Straightforward {@link EffectContext}: just holds the four things an effect is allowed to touch. */
public final class SimpleEffectContext implements EffectContext {

    private final Champion caster;
    private final Champion opponent;
    private final DamageResolver damage;
    private final EventPublisher events;
    private final boolean comboActive;

    public SimpleEffectContext(Champion caster, Champion opponent, DamageResolver damage,
                                EventPublisher events, boolean comboActive) {
        this.caster = caster;
        this.opponent = opponent;
        this.damage = damage;
        this.events = events;
        this.comboActive = comboActive;
    }

    @Override
    public Champion caster() {
        return caster;
    }

    @Override
    public Champion opponent() {
        return opponent;
    }

    @Override
    public DamageResolver damage() {
        return damage;
    }

    @Override
    public EventPublisher events() {
        return events;
    }

    @Override
    public boolean comboActive() {
        return comboActive;
    }
}
