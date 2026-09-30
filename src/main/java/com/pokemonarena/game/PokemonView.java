package com.pokemonarena.game;

import com.pokemonarena.board.PokemonInPlay;
import com.pokemonarena.cards.PokemonCard;

/** Immutable snapshot of a Pokémon on the Board at the moment the view was taken. */
public record PokemonView(PokemonCard card, int attack, int currentHp, int maxHp, boolean canAttack) {

    static PokemonView of(PokemonInPlay pokemon) {
        return new PokemonView(pokemon.card(), pokemon.attack(), pokemon.currentHp(), pokemon.maxHp(),
                pokemon.canAttack());
    }

    public String id() {
        return card.id();
    }

    public String name() {
        return card.name();
    }
}
