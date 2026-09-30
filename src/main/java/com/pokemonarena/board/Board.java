package com.pokemonarena.board;

import com.pokemonarena.cards.PokemonCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A Hero's Board: at most {@value #MAX_POKEMON} Pokémon, in the order they were played. */
public final class Board {

    public static final int MAX_POKEMON = 3;

    private final List<PokemonInPlay> pokemon = new ArrayList<>();

    public List<PokemonInPlay> pokemon() {
        return Collections.unmodifiableList(pokemon);
    }

    public int size() {
        return pokemon.size();
    }

    public boolean isEmpty() {
        return pokemon.isEmpty();
    }

    public boolean isFull() {
        return pokemon.size() >= MAX_POKEMON;
    }

    public boolean hasSlot(int index) {
        return index >= 0 && index < pokemon.size();
    }

    public PokemonInPlay at(int index) {
        if (!hasSlot(index)) {
            throw new IndexOutOfBoundsException("no Pokémon in slot " + index);
        }
        return pokemon.get(index);
    }

    /** Summons a Pokémon; it cannot attack during the turn it enters the Board. */
    public PokemonInPlay summon(PokemonCard card) {
        if (isFull()) {
            throw new IllegalStateException("the Board is full");
        }
        PokemonInPlay inPlay = new PokemonInPlay(card);
        pokemon.add(inPlay);
        return inPlay;
    }

    /** Makes every surviving Pokémon able to attack again. */
    public void refreshForNewTurn() {
        pokemon.forEach(PokemonInPlay::refreshForNewTurn);
    }

    /** Removes Pokémon at 0 HP and returns them, in Board order, for the discard pile. */
    public List<PokemonInPlay> removeDead() {
        List<PokemonInPlay> dead = new ArrayList<>();
        pokemon.removeIf(inPlay -> {
            if (inPlay.isAlive()) {
                return false;
            }
            dead.add(inPlay);
            return true;
        });
        return dead;
    }

    @Override
    public String toString() {
        return pokemon.toString();
    }
}
