package com.pokemonarena.cards.effects;

/** Raises maximum mana (capped at 10) and restores some available mana. */
public record IncreaseMaxManaEffect(int maxManaGain, int availableManaGain) implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.increaseMaxMana(maxManaGain, availableManaGain);
    }

    @Override
    public String describe() {
        return "increase maximum mana by " + maxManaGain
                + " and restore " + availableManaGain + " available mana";
    }
}
