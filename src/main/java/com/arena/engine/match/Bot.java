package com.arena.engine.match;

import com.arena.engine.cards.Card;

import java.util.List;
import java.util.Optional;

/** A playing strategy; the engine only knows this interface, so any bot can face any other. */
@FunctionalInterface
public interface Bot {

    /** Called repeatedly during the play phase until the bot returns {@link EndTurn}. */
    Action nextAction(GameView view);

    /** Name shown in logs and stats. */
    default String name() {
        return getClass().getSimpleName();
    }

    /** Positions of opening-hand cards to put back; keeping the whole hand is the safe default. */
    default List<Integer> mulligan(List<Card> openingHand) {
        return List.of();
    }

    /**
     * What the bot thought and said since the engine last asked; bots stay free of IO, so the engine
     * publishes it. Silent by default: only talking bots (LLM) override it.
     */
    default Optional<Speech> takeSpeech() {
        return Optional.empty();
    }
}
