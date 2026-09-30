package com.arena.engine.match;

/** The closed set of things a bot can do on its turn; the engine validates and applies each one. */
public sealed interface Action permits Action.PlayCard, Action.UseHeroPower, Action.EndTurn {

    /** Play the card at this position in the current hand. */
    record PlayCard(int handIndex) implements Action {
    }

    /** Activate the class hero power, if not already used this turn and affordable. */
    record UseHeroPower() implements Action {
    }

    /** Stop taking actions; the engine moves on to the resolve phase. */
    record EndTurn() implements Action {
    }
}
