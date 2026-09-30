package com.pokemonarena.cards.effects;

/** Returns the Pokémon card chosen by the player from the discard pile to the owner's hand. */
public record ReturnPokemonFromDiscardEffect() implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.returnChosenPokemonFromDiscard();
    }

    @Override
    public String describe() {
        return "return a chosen Pokémon card from the discard pile to its owner's hand";
    }
}
