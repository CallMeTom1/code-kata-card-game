package com.pokemonarena.cards;

import com.pokemonarena.cards.effects.Effect;

import java.util.Optional;

/**
 * Definition of a Pokémon card: printed stats plus an optional "when played" ability.
 * Abilities trigger once when the Pokémon enters the board; they are never persistent.
 */
public record PokemonCard(String id,
                          String name,
                          int manaCost,
                          CardCategory category,
                          int attack,
                          int maxHp,
                          Effect whenPlayed) implements Card {

    public PokemonCard {
        if (manaCost < 0) {
            throw new IllegalArgumentException("mana cost cannot be negative: " + id);
        }
        if (attack < 0) {
            throw new IllegalArgumentException("attack cannot be negative: " + id);
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("max HP must be positive: " + id);
        }
    }

    public PokemonCard(String id, String name, int manaCost, CardCategory category, int attack, int maxHp) {
        this(id, name, manaCost, category, attack, maxHp, null);
    }

    @Override
    public CardNature nature() {
        return CardNature.POKEMON;
    }

    /** The "when played" ability, if the Pokémon has one. */
    public Optional<Effect> ability() {
        return Optional.ofNullable(whenPlayed);
    }

    @Override
    public String text() {
        return ability().map(effect -> "When played: " + effect.describe() + ".").orElse("");
    }
}
