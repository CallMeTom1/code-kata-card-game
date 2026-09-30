package com.arena.engine.combat;

import com.arena.engine.board.Minion;
import com.arena.engine.player.Champion;

/** Single entry point for damage, so defensive rules live in one place. */
public interface DamageResolver {

    /** Normal damage to a champion: Evasion, then armor, then Parry. */
    void deal(String source, Champion target, int amount);

    /** Damage that goes through armor (Eviscerate); Evasion and Parry still apply. */
    void dealIgnoringArmor(String source, Champion target, int amount);

    /** Damage to a minion; removes it from its owner's board when it dies. */
    void damageMinion(String source, Champion owner, Minion minion, int amount);
}
