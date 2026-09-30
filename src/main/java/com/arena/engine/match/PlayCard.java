package com.arena.engine.match;

/** Plays the card at this position in the hand; the engine checks the mana. */
public record PlayCard(int handIndex) implements Action {

    /** Rejects a negative index at once, so a buggy bot fails loudly. */
    public PlayCard {
        if (handIndex < 0) {
            throw new IllegalArgumentException("Hand index cannot be negative: " + handIndex);
        }
    }
}
