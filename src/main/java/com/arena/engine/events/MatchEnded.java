package com.arena.engine.events;

/** Closes the log; winner is "DRAW" when nobody won. Damage is HP the other side lost (no fatigue). */
public record MatchEnded(String winner, String reason, int rounds, String player1, int hp1, int damage1, String player2, int hp2, int damage2) implements GameEvent {
}
