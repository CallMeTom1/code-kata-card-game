package com.arena.engine.combat;

/** A value that lasts a number of its owner's turns (one armor layer, one Parry). */
final class TimedAmount {

    int amount;
    int turnsLeft;

    TimedAmount(int amount, int turnsLeft) {
        this.amount = amount;
        this.turnsLeft = turnsLeft;
    }
}
