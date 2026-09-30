package com.arena.engine.match;

/** The final outcome of a match: who won (or null for a draw), why, and how long it took. */
public record MatchResult(String winner, String reason, int turns, int damageByPlayer1, int damageByPlayer2) {
}
