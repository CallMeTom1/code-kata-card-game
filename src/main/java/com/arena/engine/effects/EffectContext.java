package com.arena.engine.effects;

import com.arena.engine.combat.DamageResolver;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** What an effect may use, and nothing more, so effects stay decoupled from the match (Interface Segregation). */
public interface EffectContext {

    /** The champion who played the card, target of self effects like heal or armor. */
    Champion caster();

    /** The enemy champion, target of damage effects. */
    Champion opponent();

    /** All damage goes through here so armor, Parry and Taunt rules live in one place. */
    DamageResolver damage();

    /** Lets effects report what they did, so the log shows every change. */
    EventPublisher events();
}
