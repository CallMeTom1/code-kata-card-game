package com.pokemonarena.cards.effects;

/**
 * Deals damage to the opposing Hero, increased when that Hero is already low on HP
 * (Sulfura's Flame Burst).
 */
public record ThresholdDamageHeroEffect(int amount, int hpThreshold, int boostedAmount) implements Effect {

    @Override
    public void apply(EffectContext context) {
        int damage = context.opposingHeroHp() <= hpThreshold ? boostedAmount : amount;
        context.damageOpposingHero(damage, true);
    }

    @Override
    public String describe() {
        return "deal " + amount + " damage to the opposing Hero, or " + boostedAmount
                + " if it has " + hpThreshold + " HP or less";
    }
}
