package com.pokemonarena.cards.effects;

/** Restores available mana, never above maximum mana. */
public record RestoreManaEffect(int amount) implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.restoreAvailableMana(amount);
    }

    @Override
    public String describe() {
        return "restore " + amount + " available mana";
    }
}
