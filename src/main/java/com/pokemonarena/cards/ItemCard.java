package com.pokemonarena.cards;

import com.pokemonarena.cards.effects.Effect;

import java.util.Objects;

/**
 * Definition of an Item card. Items resolve their effect immediately and then go to the
 * discard pile; they never occupy a board slot.
 */
public record ItemCard(String id,
                       String name,
                       int manaCost,
                       CardCategory category,
                       Effect effect,
                       Target target) implements Card {

    /** What the {@code targetIndex} of a played Item refers to. */
    public enum Target {
        /** No target. */
        NONE,
        /** A slot of the opposing Board (Rock Throw). */
        OPPOSING_POKEMON,
        /** A Pokémon card in the owner's discard pile, by discard index (Rappel). */
        DISCARDED_POKEMON
    }

    public ItemCard {
        Objects.requireNonNull(effect, "an Item must have an effect: " + id);
        Objects.requireNonNull(target, "an Item must declare its target: " + id);
        if (manaCost < 0) {
            throw new IllegalArgumentException("mana cost cannot be negative: " + id);
        }
    }

    public ItemCard(String id, String name, int manaCost, CardCategory category, Effect effect) {
        this(id, name, manaCost, category, effect, Target.NONE);
    }

    public boolean requiresPokemonTarget() {
        return target == Target.OPPOSING_POKEMON;
    }

    @Override
    public CardNature nature() {
        return CardNature.ITEM;
    }

    @Override
    public String text() {
        return capitalize(effect.describe()) + ".";
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
