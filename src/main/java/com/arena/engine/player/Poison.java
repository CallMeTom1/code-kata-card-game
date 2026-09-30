package com.arena.engine.player;

/** One stack of poison; ignores armor, ticks at the start of each of the victim's next turns, then expires. */
public record Poison(int amount, int turnsLeft) {

    public Poison ticked() {
        return new Poison(amount, turnsLeft - 1);
    }

    public boolean expired() {
        return turnsLeft <= 0;
    }
}
