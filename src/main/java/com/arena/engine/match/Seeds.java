package com.arena.engine.match;

import java.util.Random;
import java.util.SplittableRandom;

/**
 * Creates the seeded Random used everywhere. java.util.Random gives almost the same first
 * values for consecutive seeds (42, 43, …), which made player 1 always start; mixing the seed
 * first keeps matches reproducible and the coin flip fair.
 */
public final class Seeds {

    private Seeds() {
    }

    /** A Random that depends only on the seed, with well-spread first values. */
    public static Random random(long seed) {
        return new Random(new SplittableRandom(seed).nextLong());
    }
}
