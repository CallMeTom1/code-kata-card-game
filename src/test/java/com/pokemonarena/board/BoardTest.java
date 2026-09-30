package com.pokemonarena.board;

import com.pokemonarena.cards.CardCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void holdsAtMostThreePokemon() {
        Board board = new Board();
        board.summon(CardCatalog.PIKACHU);
        board.summon(CardCatalog.PIKACHU);
        board.summon(CardCatalog.PIKACHU);

        assertTrue(board.isFull());
        assertThrows(IllegalStateException.class, () -> board.summon(CardCatalog.PIKACHU));
    }

    @Test
    void aSummonedPokemonCannotAttackBeforeItsOwnersNextTurn() {
        Board board = new Board();
        PokemonInPlay pikachu = board.summon(CardCatalog.PIKACHU);

        assertFalse(pikachu.canAttack());

        board.refreshForNewTurn();
        assertTrue(pikachu.canAttack());

        pikachu.markAttacked();
        assertFalse(pikachu.canAttack());
    }

    @Test
    void deadPokemonAreRemovedInBoardOrderAndHpNeverGoesNegative() {
        Board board = new Board();
        PokemonInPlay pikachu = board.summon(CardCatalog.PIKACHU);
        PokemonInPlay squirtle = board.summon(CardCatalog.SQUIRTLE);

        pikachu.receiveDamage(99);

        assertEquals(0, pikachu.currentHp());
        assertFalse(pikachu.isAlive());

        List<PokemonInPlay> dead = board.removeDead();

        assertEquals(List.of(pikachu), dead);
        assertEquals(List.of(squirtle), board.pokemon());
    }

    @Test
    void aDeadPokemonCanNeverAttack() {
        Board board = new Board();
        PokemonInPlay pikachu = board.summon(CardCatalog.PIKACHU);
        board.refreshForNewTurn();
        pikachu.receiveDamage(2);

        assertFalse(pikachu.canAttack());
    }
}
