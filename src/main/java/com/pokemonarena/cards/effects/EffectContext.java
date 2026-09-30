package com.pokemonarena.cards.effects;

/**
 * Everything an {@link Effect} is allowed to do to the game state.
 * <p>
 * The engine (implemented in a later pass) provides the concrete implementation; effects
 * themselves never touch the game state directly. "Self" always means the Hero that played
 * the card or used the Hero Power.
 */
public interface EffectContext {

    /** Current HP of the opposing Hero, used by conditional effects. */
    int opposingHeroHp();

    /**
     * Deals damage to the opposing Hero. Pending attack buffs are applied by the engine when
     * {@code fromAttackEffect} is {@code true}.
     */
    void damageOpposingHero(int amount, boolean fromAttackEffect);

    /**
     * Deals damage to the opposing Pokémon targeted by the action being resolved.
     * Does nothing if there is no such target.
     */
    void damageTargetPokemon(int amount, boolean fromAttackEffect);

    /** Heals the Hero that played the card, never above its maximum HP. */
    void healSelfHero(int amount);

    /** Draws {@code count} cards for the Hero that played the card. */
    void drawCards(int count);

    /** Restores available mana, never above maximum mana. */
    void restoreAvailableMana(int amount);

    /** Raises maximum mana (capped at 10) and restores some available mana. */
    void increaseMaxMana(int maxManaGain, int availableManaGain);

    /**
     * Queues a "next damage received" reduction on the Hero.
     *
     * @param amount                    how much damage is prevented
     * @param expiresAtEndOfFollowingTurn {@code true} for Frozen Barrier-like effects, which are
     *                                    lost if they were not consumed in time
     */
    void addHeroDamageReduction(int amount, boolean expiresAtEndOfFollowingTurn);

    /**
     * Queues a "next Attack effect deals +X damage" buff on the Hero.
     *
     * @param thisTurnOnly {@code true} if the buff is lost at the end of the current turn
     */
    void addAttackBuff(int amount, boolean thisTurnOnly);

    /**
     * Returns the Pokémon card chosen by the action from the Hero's discard pile to its hand.
     * Does nothing if no Pokémon was chosen (the discard pile contains no Pokémon).
     */
    void returnChosenPokemonFromDiscard();
}
