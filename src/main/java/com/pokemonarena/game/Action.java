package com.pokemonarena.game;

/**
 * An action a Hero may perform during the PLAY phase. Actions are pure intents: they are
 * produced outside the engine (by Bots later on) and only the engine mutates the state.
 */
public sealed interface Action {

    /** Target index meaning "the opposing Hero" / "no Pokémon target". */
    int NO_POKEMON_TARGET = -1;

    /**
     * Plays a card from the hand.
     *
     * @param handIndex   position of the card in the hand
     * @param targetIndex slot of the targeted opposing Pokémon (Rock Throw), index of the chosen
     *                    Pokémon in the Hero's own discard pile (Rappel), or {@link #NO_POKEMON_TARGET}
     */
    record PlayCard(int handIndex, int targetIndex) implements Action {

        public PlayCard(int handIndex) {
            this(handIndex, NO_POKEMON_TARGET);
        }
    }

    /** Uses the Hero Power, at most once per turn. */
    record UseHeroPower() implements Action {
    }

    /**
     * Attacks with one of the Hero's Pokémon.
     *
     * @param attackerIndex slot of the attacking Pokémon
     * @param targetIndex   slot of the targeted opposing Pokémon, or {@link #NO_POKEMON_TARGET}
     *                      to attack the opposing Hero
     */
    record Attack(int attackerIndex, int targetIndex) implements Action {

        public static Attack onHero(int attackerIndex) {
            return new Attack(attackerIndex, NO_POKEMON_TARGET);
        }
    }

    /** Stops playing and ends the turn. */
    record EndTurn() implements Action {
    }
}
