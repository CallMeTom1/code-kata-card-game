package com.pokemonarena.cards.effects;

/** Heals the Hero that played the card. */
public record HealHeroEffect(int amount) implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.healSelfHero(amount);
    }

    @Override
    public String describe() {
        return "heal its Hero for " + amount + " HP";
    }
}
