package com.pokemonarena.cards.effects;

/** Draws cards for the Hero that played the card. */
public record DrawEffect(int count) implements Effect {

    @Override
    public void apply(EffectContext context) {
        context.drawCards(count);
    }

    @Override
    public String describe() {
        return "draw " + count + (count == 1 ? " card" : " cards");
    }
}
