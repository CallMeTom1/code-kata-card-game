package com.pokemonarena.cards.effects;

import java.util.List;

/** Applies several effects in order, so cards and Hero Powers can be composed instead of subclassed. */
public record CompositeEffect(List<Effect> effects) implements Effect {

    public CompositeEffect(Effect... effects) {
        this(List.of(effects));
    }

    public CompositeEffect {
        effects = List.copyOf(effects);
    }

    @Override
    public void apply(EffectContext context) {
        effects.forEach(effect -> effect.apply(context));
    }

    @Override
    public String describe() {
        return String.join(", then ", effects.stream().map(Effect::describe).toList());
    }
}
