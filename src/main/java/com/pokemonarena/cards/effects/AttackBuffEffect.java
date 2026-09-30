package com.pokemonarena.cards.effects;

/**
 * Queues a "the next Attack effect deals +X damage" buff on the Hero. Buffs stack additively,
 * are consumed by the first applicable damage event and never modify printed Pokémon stats.
 */
public record AttackBuffEffect(int amount, boolean thisTurnOnly) implements Effect {

    public AttackBuffEffect(int amount) {
        this(amount, false);
    }

    @Override
    public void apply(EffectContext context) {
        context.addAttackBuff(amount, thisTurnOnly);
    }

    @Override
    public String describe() {
        String text = "the next Attack effect deals +" + amount + " damage";
        return thisTurnOnly ? text + " this turn" : text;
    }
}
