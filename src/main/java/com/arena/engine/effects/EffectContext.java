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

    /** All damage goes through here so armor, Parry and Evasion rules live in one place. */
    DamageResolver damage();

    /** Lets effects report what they did, so the log shows every change. */
    EventPublisher events();

    /** Name of the card or hero power being resolved, used as the source in logs. */
    String source();

    /** True when another card was played earlier this turn (The Coin counts, the hero power does not). */
    boolean comboActive();

    /** "Next Attack +X" bonus taken by this card; 0 for anything that is not a damaging Attack card. */
    int attackBonus();

    /** Current round, needed for summoning sickness. */
    int round();

    /** Draws with the Hearthstone rules (burn, fatigue). */
    void draw(Champion who, int count);
}
