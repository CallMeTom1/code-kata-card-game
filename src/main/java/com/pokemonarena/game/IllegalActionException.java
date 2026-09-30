package com.pokemonarena.game;

/**
 * Thrown when an action is rejected by the engine. The game state is left untouched: an
 * illegal play never consumes mana nor the card.
 */
public class IllegalActionException extends RuntimeException {

    public IllegalActionException(String message) {
        super(message);
    }
}
