package com.pokemonarena.game;

import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pokemonarena.game.MatchFixture.advanceToTurn;
import static com.pokemonarena.game.MatchFixture.match;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per Hero Power, plus the shared cost and once-per-turn rules. */
class HeroPowerTest {

    private static Match matchFor(com.pokemonarena.hero.Hero hero) {
        return match(hero, List.of(), HeroCatalog.ZAPDOS, List.of());
    }

    @Test
    void staticChargeDealsTwoDamageToTheOpposingHero() {
        Match match = matchFor(HeroCatalog.ZAPDOS);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());

        assertEquals(28, match.player2().currentHp());
        assertEquals(0, match.player1().availableMana());
    }

    @Test
    void frozenBarrierReducesTheNextDamageReceivedByFour() {
        Match match = matchFor(HeroCatalog.ARTICUNO);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());
        match.player1().receiveDamage(6);

        assertEquals(28, match.player1().currentHp());
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void flameBurstDealsThreeDamageAgainstAHealthyHero() {
        Match match = matchFor(HeroCatalog.MOLTRES);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());

        assertEquals(27, match.player2().currentHp());
    }

    @Test
    void flameBurstDealsFourDamageAgainstAHeroAtTenHpOrLess() {
        Match match = matchFor(HeroCatalog.MOLTRES);
        match.player2().receiveDamage(20);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());

        assertEquals(6, match.player2().currentHp());
    }

    @Test
    void aeroblastDealsTwoDamageAndDrawsACard() {
        Match match = matchFor(HeroCatalog.LUGIA);
        advanceToTurn(match, 3);
        int handSize = match.player1().hand().size();

        match.perform(new Action.UseHeroPower());

        assertEquals(28, match.player2().currentHp());
        assertEquals(handSize + 1, match.player1().hand().size());
    }

    @Test
    void sacredFlameHealsThreeAndQueuesATwoDamageAttackBuff() {
        Match match = matchFor(HeroCatalog.HO_OH);
        match.player1().receiveDamage(10);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());

        assertEquals(23, match.player1().currentHp());
        assertEquals(1, match.player1().temporaryEffects().size());
        TemporaryEffect buff = match.player1().temporaryEffects().get(0);
        assertEquals(TemporaryEffect.Kind.ATTACK_BUFF, buff.kind());
        assertEquals(2, buff.value());
    }

    @Test
    void aHeroPowerCannotBeUsedWithoutEnoughMana() {
        Match match = matchFor(HeroCatalog.ZAPDOS);
        advanceToTurn(match, 1);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.UseHeroPower()));

        assertEquals(1, match.player1().availableMana());
        assertEquals(30, match.player2().currentHp());
    }
}
