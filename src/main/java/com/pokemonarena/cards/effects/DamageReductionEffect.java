package com.pokemonarena.cards.effects;

/**
 * Queues a "reduce the next damage received by the Hero" effect. Several reductions can apply
 * to the same damage event, stack additively and are consumed oldest first.
 */
public record DamageReductionEffect(int amount, boolean expiresAtEndOfFollowingTurn) implements Effect {

    public DamageReductionEffect(int amount) {
        this(amount, false);
    }

    @Override
    public void apply(EffectContext context) {
        context.addHeroDamageReduction(amount, expiresAtEndOfFollowingTurn);
    }

    @Override
    public String describe() {
        String text = "reduce the next damage received by its Hero by " + amount;
        return expiresAtEndOfFollowingTurn
                ? text + " (expires at the end of the following turn)"
                : text;
    }
}
