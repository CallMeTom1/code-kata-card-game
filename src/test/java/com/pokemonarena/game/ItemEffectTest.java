package com.pokemonarena.game;

import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pokemonarena.game.MatchFixture.advanceToTurn;
import static com.pokemonarena.game.MatchFixture.match;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per Item effect, plus target and cost validation. */
class ItemEffectTest {

    private static Match matchWith(com.pokemonarena.cards.Card... playerOneCards) {
        return match(HeroCatalog.ZAPDOS, List.of(playerOneCards), HeroCatalog.ARTICUNO, List.of());
    }

    @Test
    void superPotionHealsEight() {
        Match match = matchWith(CardCatalog.SUPER_POTION);
        match.player1().receiveDamage(10);
        advanceToTurn(match, 5);

        match.perform(new Action.PlayCard(0));

        assertEquals(28, match.player1().currentHp());
    }

    @Test
    void hyperPotionHealsTwelveWithoutExceedingTheStartingHp() {
        Match match = matchWith(CardCatalog.HYPER_POTION);
        match.player1().receiveDamage(5);
        advanceToTurn(match, 9);

        match.perform(new Action.PlayCard(0));

        assertEquals(30, match.player1().currentHp());
    }

    @Test
    void pokeBallDrawsTwoCards() {
        Match match = matchWith(CardCatalog.POKE_BALL);
        advanceToTurn(match, 3);
        int handSize = match.player1().hand().size();

        match.perform(new Action.PlayCard(0));

        assertEquals(handSize + 1, match.player1().hand().size());
    }

    @Test
    void energyRestoresAvailableManaUpToTheMaximum() {
        Match match = matchWith(CardCatalog.ENERGY);
        advanceToTurn(match, 5);

        match.perform(new Action.PlayCard(0));

        assertEquals(3, match.player1().maxMana());
        assertEquals(3, match.player1().availableMana());
    }

    @Test
    void superBonbonRaisesMaximumManaAndRestoresOne() {
        Match match = matchWith(CardCatalog.SUPER_BONBON);
        advanceToTurn(match, 3);

        match.perform(new Action.PlayCard(0));

        assertEquals(3, match.player1().maxMana());
        assertEquals(1, match.player1().availableMana());
    }

    @Test
    void defenseXReducesTheNextDamageByFour() {
        Match match = matchWith(CardCatalog.DEFENSE_X);
        advanceToTurn(match, 3);

        match.perform(new Action.PlayCard(0));
        match.player1().receiveDamage(6);

        assertEquals(28, match.player1().currentHp());
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void reductionsStackAdditivelyOnTheSameDamageEvent() {
        Match match = matchWith(CardCatalog.DEFENSE_X, CardCatalog.PROTECTION);
        advanceToTurn(match, 11);

        match.perform(new Action.PlayCard(0));
        match.perform(new Action.PlayCard(0));
        assertEquals(2, match.player1().temporaryEffects().size());

        match.player1().receiveDamage(12);

        assertEquals(29, match.player1().currentHp());
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void onlyTheReductionsNeededToAbsorbTheDamageAreConsumedOldestFirst() {
        Match match = matchWith(CardCatalog.DEFENSE_X, CardCatalog.PROTECTION);
        advanceToTurn(match, 11);

        match.perform(new Action.PlayCard(0));
        match.perform(new Action.PlayCard(0));
        match.player1().receiveDamage(3);

        assertEquals(30, match.player1().currentHp());
        assertEquals(1, match.player1().temporaryEffects().size());
        assertEquals("protection", match.player1().temporaryEffects().get(0).source());
    }

    @Test
    void fireBlastDealsFourDamageToTheOpposingHero() {
        Match match = matchWith(CardCatalog.FIRE_BLAST);
        advanceToTurn(match, 5);

        match.perform(new Action.PlayCard(0));

        assertEquals(26, match.player2().currentHp());
        assertEquals(4, match.player1().damageDealtToOpposingHero());
    }

    @Test
    void thunderShockDealsTwoDamageToTheOpposingHero() {
        Match match = matchWith(CardCatalog.THUNDER_SHOCK);
        advanceToTurn(match, 3);

        match.perform(new Action.PlayCard(0));

        assertEquals(28, match.player2().currentHp());
    }

    @Test
    void rappelReturnsTheChosenPokemonOfTheDiscardPileToTheHand() {
        Match match = matchWith(CardCatalog.RAPPEL);
        match.player1().discard(CardCatalog.CHARIZARD);
        match.player1().discard(CardCatalog.SQUIRTLE);
        advanceToTurn(match, 7);
        int handSize = match.player1().hand().size();

        // Choose Squirtle (index 1) although Charizard costs more.
        match.perform(new Action.PlayCard(0, 1));

        assertEquals(handSize, match.player1().hand().size());
        assertTrue(match.player1().hand().contains(CardCatalog.SQUIRTLE));
        assertEquals(List.of(CardCatalog.CHARIZARD, CardCatalog.RAPPEL), match.player1().discard());
        assertEquals(0, match.player1().availableMana());
    }

    @Test
    void rappelRejectsADiscardTargetThatIsNotAPokemonAndIsNotConsumed() {
        Match match = matchWith(CardCatalog.RAPPEL);
        match.player1().discard(CardCatalog.POTION);
        match.player1().discard(CardCatalog.PIKACHU);
        advanceToTurn(match, 7);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0, 0)));
        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0, 5)));
        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0)));

        assertEquals(CardCatalog.RAPPEL, match.player1().hand().get(0));
        assertEquals(List.of(CardCatalog.POTION, CardCatalog.PIKACHU), match.player1().discard());
        assertEquals(4, match.player1().availableMana());
    }

    @Test
    void rappelCannotBePlayedWithoutEnoughMana() {
        Match match = matchWith(CardCatalog.RAPPEL);
        match.player1().discard(CardCatalog.PIKACHU);
        advanceToTurn(match, 5);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0, 0)));

        assertEquals(CardCatalog.RAPPEL, match.player1().hand().get(0));
        assertEquals(List.of(CardCatalog.PIKACHU), match.player1().discard());
        assertEquals(3, match.player1().availableMana());
    }

    @Test
    void rappelIsStillPlayedAndSpendsManaWhenTheDiscardPileHasNoPokemon() {
        Match match = matchWith(CardCatalog.RAPPEL);
        advanceToTurn(match, 7);
        int handSize = match.player1().hand().size();

        match.perform(new Action.PlayCard(0));

        assertEquals(handSize - 1, match.player1().hand().size());
        assertEquals(0, match.player1().availableMana());
        assertEquals(List.of(CardCatalog.RAPPEL), match.player1().discard());
    }

    @Test
    void anItemCannotBePlayedWithoutEnoughManaAndStaysInHand() {
        Match match = matchWith(CardCatalog.HYPER_POTION);
        advanceToTurn(match, 3);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0)));

        assertEquals(CardCatalog.HYPER_POTION, match.player1().hand().get(0));
        assertEquals(2, match.player1().availableMana());
    }

    @Test
    void anInvalidHandIndexIsRejected() {
        Match match = matchWith();
        advanceToTurn(match, 3);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(42)));
        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(-1)));
    }

    @Test
    void rockThrowCannotTargetAnEmptySlot() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.ROCK_THROW),
                HeroCatalog.ARTICUNO, List.of(CardCatalog.SQUIRTLE));
        advanceToTurn(match, 4);
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0, 2)));
        assertEquals(CardCatalog.ROCK_THROW, match.player1().hand().get(0));
    }
}
