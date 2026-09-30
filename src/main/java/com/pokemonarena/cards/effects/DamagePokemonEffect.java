package com.pokemonarena.cards.effects;

/** Deals damage to the opposing Pokémon targeted by the action. */
public record DamagePokemonEffect(int amount) implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.damageTargetPokemon(amount, true);
    }

    @Override
    public String describe() {
        return "deal " + amount + " damage to an opposing Pokémon";
    }
}
