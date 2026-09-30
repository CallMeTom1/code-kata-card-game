package com.arena.stats;

import com.arena.engine.match.MatchResult;

/** One match of a batch with its seed and the classes played, for the stats export. */
public record MatchRecord(long seed, String class1, String class2, MatchResult result) {
}
