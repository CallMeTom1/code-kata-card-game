package com.pokemonarena.cards.effects;

/** Deals damage to the opposing Hero. */
public record DamageHeroEffect(int amount, boolean fromAttackEffect) implements Effect {

    public DamageHeroEffect(int amount) {
        this(amount, true);
    }

    @Override
    public void apply(EffectContext context) {
        context.damageOpposingHero(amount, fromAttackEffect);
    }

    @Override
    public String describe() {
        return "deal " + amount + " damage to the opposing Hero";
    }
}
