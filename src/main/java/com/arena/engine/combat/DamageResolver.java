package com.arena.engine.combat;

import com.arena.engine.player.Champion;

/** Single entry point for damage, so defensive rules can be added by swapping the implementation. */
public interface DamageResolver {

    /** Applies damage from a named source (card, minion, hero power) to a champion. */
    void deal(String source, Champion target, int amount);
}
