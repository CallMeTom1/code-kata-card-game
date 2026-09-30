package com.arena.engine.board;

/** Passive abilities a minion can have; closed list so the engine handles each one explicitly. */
public sealed interface MinionAbility {

    /** Most minions just attack. */
    record None() implements MinionAbility {
    }

    /** Spirit Healer: heals its owner at the end of each of the owner's turns. */
    record HealOwnerAtEndOfTurn(int amount) implements MinionAbility {
    }

    /** Venom Spider: its hits on the enemy champion also poison. */
    record PoisonOnHit(int amount, int turns) implements MinionAbility {
    }
}
