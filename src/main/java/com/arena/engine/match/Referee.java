package com.arena.engine.match;

/** The end-of-match rules of DESIGN.md, kept apart from the turn loop (Single Responsibility). */
public final class Referee {

    private Referee() {
    }

    /** Called as soon as a champion reaches 0 HP; both at 0 is a draw. */
    public static Verdict onDeath(String player1, boolean player1Dead, String player2, boolean player2Dead) {
        if (player1Dead && player2Dead) {
            return new Verdict(null, "both at 0 HP");
        }
        return new Verdict(player1Dead ? player2 : player1, "HP 0");
    }

    /** After 50 rounds: more HP wins, then more damage dealt, else draw. */
    public static Verdict tieBreak(String player1, int hp1, int damage1, String player2, int hp2, int damage2) {
        if (hp1 != hp2) {
            return new Verdict(hp1 > hp2 ? player1 : player2, "turn limit, more HP");
        }
        if (damage1 != damage2) {
            return new Verdict(damage1 > damage2 ? player1 : player2, "turn limit, more damage dealt");
        }
        return new Verdict(null, "turn limit, full tie");
    }
}
