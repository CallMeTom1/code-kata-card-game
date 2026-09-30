package com.pokemonarena.game;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStateTest {

    private PlayerState state(Card... deck) {
        return new PlayerState(HeroCatalog.ZAPDOS, new Deck(List.of(deck)));
    }

    @Test
    void maximumManaGrowsByOnePerTurnAndIsCappedAtTen() {
        PlayerState player = state();

        for (int turn = 1; turn <= 12; turn++) {
            player.gainMana();
        }

        assertEquals(10, player.maxMana());
        assertEquals(10, player.availableMana());
    }

    @Test
    void availableManaIsRefilledEveryTurnAndNeverGoesNegative() {
        PlayerState player = state();
        player.gainMana();
        player.gainMana();
        player.spendMana(2);

        assertEquals(0, player.availableMana());
        assertThrows(IllegalArgumentException.class, () -> player.spendMana(1));

        player.gainMana();
        assertEquals(3, player.availableMana());
    }

    @Test
    void resourceEffectsNeverPushManaAboveTheCap() {
        PlayerState player = state();
        for (int turn = 1; turn <= 10; turn++) {
            player.gainMana();
        }

        player.increaseMaxMana(1, 1);

        assertEquals(10, player.maxMana());
        assertEquals(10, player.availableMana());
    }

    @Test
    void drawingFromAnEmptyDeckDoesNothing() {
        PlayerState player = state(CardCatalog.PIKACHU);

        assertTrue(player.drawCard().isPresent());
        assertTrue(player.drawCard().isEmpty());
        assertEquals(1, player.hand().size());
    }

    @Test
    void healingNeverGoesAboveThirtyHp() {
        PlayerState player = state();
        player.receiveDamage(5);

        player.heal(10);

        assertEquals(30, player.currentHp());
    }

    @Test
    void damageReductionsStackAdditivelyAndAreConsumedOldestFirst() {
        PlayerState player = state();
        player.addTemporaryEffect(TemporaryEffect.damageReduction("onix", 2, TemporaryEffect.NEVER_EXPIRES));
        player.addTemporaryEffect(TemporaryEffect.damageReduction("defense-x", 4, TemporaryEffect.NEVER_EXPIRES));

        assertEquals(1, player.receiveDamage(7));
        assertEquals(29, player.currentHp());
        assertTrue(player.temporaryEffects().isEmpty());
    }

    @Test
    void aReductionLargeEnoughAloneLeavesTheNextOneQueued() {
        PlayerState player = state();
        player.addTemporaryEffect(TemporaryEffect.damageReduction("protection", 7, TemporaryEffect.NEVER_EXPIRES));
        player.addTemporaryEffect(TemporaryEffect.damageReduction("defense-x", 4, TemporaryEffect.NEVER_EXPIRES));

        assertEquals(0, player.receiveDamage(3));
        assertEquals(30, player.currentHp());
        assertEquals(List.of("defense-x"),
                player.temporaryEffects().stream().map(TemporaryEffect::source).toList());
    }

    @Test
    void attackBuffsStackAdditivelyAndAreAllConsumedAtOnce() {
        PlayerState player = state();
        player.addTemporaryEffect(TemporaryEffect.attackBuff("abra", 3, TemporaryEffect.NEVER_EXPIRES));
        player.addTemporaryEffect(TemporaryEffect.attackBuff("ho-oh", 2, TemporaryEffect.NEVER_EXPIRES));

        assertEquals(5, player.consumeAttackBuffs());
        assertEquals(0, player.consumeAttackBuffs());
    }

    @Test
    void rappelReturnsTheChosenPokemonOfTheDiscardPile() {
        PlayerState player = state();
        player.discard(CardCatalog.POTION);
        player.discard(CardCatalog.PIKACHU);
        player.discard(CardCatalog.CHARIZARD);

        player.returnPokemonFromDiscard(1);

        assertEquals(List.of(CardCatalog.PIKACHU), player.hand());
        assertEquals(List.of(CardCatalog.POTION, CardCatalog.CHARIZARD), player.discard());
        assertThrows(IllegalArgumentException.class, () -> player.returnPokemonFromDiscard(0));
    }
}
