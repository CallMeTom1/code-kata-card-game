package com.arena.engine.events;

/**
 * Something that happened during a match. Sealed so every renderer can switch over all
 * events exhaustively and the compiler flags any event a renderer forgot.
 */
public sealed interface GameEvent
        permits MatchStarted, TurnStarted, CardPlayed, DamageDealt, MatchEnded {
}
