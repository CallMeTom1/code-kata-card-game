package com.arena.engine.match;

import com.arena.engine.player.Champion;

/** Decides the winner after the 50-turn limit, per DESIGN.md's tie-break rules (kept out of Match for SRP). */
public final class TieBreaker {

    /** Higher HP wins; if equal, more total damage dealt wins; if still equal, it is a draw (null winner). */
    public String decide(Champion p1, Champion p2, int damageByP1, int damageByP2) {
        if (p1.hp() != p2.hp()) {
            return p1.hp() > p2.hp() ? p1.name() : p2.name();
        }
        if (damageByP1 != damageByP2) {
            return damageByP1 > damageByP2 ? p1.name() : p2.name();
        }
        return null;
    }
}
