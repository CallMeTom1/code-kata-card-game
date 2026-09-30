package com.arena.engine.combat;

import com.arena.engine.player.Champion;

/** The single entry point for turning raw damage into HP loss, so armor/Parry rules live in one place. */
public interface DamageResolver {

    /** Deals {@code amount} damage from {@code source} to {@code target}, honoring armor and Parry. */
    void deal(String source, Champion target, int amount);

    /** Deals damage that skips armor and Parry entirely (Eviscerate's Combo bonus, poison ticks). */
    void dealIgnoringArmor(String source, Champion target, int amount);
}
