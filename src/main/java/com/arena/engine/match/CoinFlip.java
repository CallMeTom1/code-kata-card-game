package com.arena.engine.match;

import java.util.Random;

/** Decides who goes first; injectable so tests can fix it (Dependency Inversion). */
@FunctionalInterface
public interface CoinFlip {

    /** A seeded coin flip, as in DESIGN.md. */
    CoinFlip RANDOM = Random::nextBoolean;

    /** Always player 1, for tests. */
    CoinFlip PLAYER_1_FIRST = random -> true;

    /** True when player 1 goes first. */
    boolean player1First(Random random);
}
