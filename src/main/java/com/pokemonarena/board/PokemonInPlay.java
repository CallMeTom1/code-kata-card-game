package com.pokemonarena.board;

import com.pokemonarena.cards.PokemonCard;

import java.util.Objects;

/**
 * Mutable in-play state of a Pokémon occupying a Board slot: current HP and attack
 * availability. The printed stats stay in the immutable {@link PokemonCard} definition.
 */
public final class PokemonInPlay {

    private final PokemonCard card;
    private int currentHp;
    private boolean readyToAttack;

    public PokemonInPlay(PokemonCard card) {
        this.card = Objects.requireNonNull(card, "card");
        this.currentHp = card.maxHp();
        this.readyToAttack = false;
    }

    public PokemonCard card() {
        return card;
    }

    public String id() {
        return card.id();
    }

    public String name() {
        return card.name();
    }

    public int attack() {
        return card.attack();
    }

    public int maxHp() {
        return card.maxHp();
    }

    public int currentHp() {
        return currentHp;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    /** Applies damage; HP never becomes negative. Returns the damage actually taken. */
    public int receiveDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("damage cannot be negative: " + amount);
        }
        int taken = Math.min(amount, currentHp);
        currentHp -= taken;
        return taken;
    }

    /** A Pokémon can attack once per turn, and never on the turn it was played. */
    public boolean canAttack() {
        return readyToAttack && isAlive();
    }

    /** Called at the beginning of its owner's turn. */
    public void refreshForNewTurn() {
        readyToAttack = true;
    }

    public void markAttacked() {
        readyToAttack = false;
    }

    @Override
    public String toString() {
        return name() + " " + attack() + "/" + currentHp;
    }
}
