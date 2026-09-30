package com.arena.engine.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One side's minions, oldest first, which is also their attack order. */
public final class Board {

    /** Board size, as in Hearthstone. */
    public static final int MAX_MINIONS = 7;

    private final List<Minion> minions = new ArrayList<>();

    /** Places a minion unless the board is full, in which case the summon fizzles. */
    public Optional<Minion> summon(MinionTemplate template, int round) {
        if (isFull()) {
            return Optional.empty();
        }
        Minion minion = new Minion(template, round);
        minions.add(minion);
        return Optional.of(minion);
    }

    /** Read-only copy, oldest first. */
    public List<Minion> minions() {
        return List.copyOf(minions);
    }

    /** Removes a dead minion. */
    public void remove(Minion minion) {
        minions.remove(minion);
    }

    /** The Taunt minion enemy minions must attack, if any. */
    public Optional<Minion> firstTaunt() {
        return minions.stream().filter(Minion::taunt).findFirst();
    }

    /** True at 7 minions. */
    public boolean isFull() {
        return minions.size() >= MAX_MINIONS;
    }
}
