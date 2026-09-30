package com.pokemonarena.game;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.deck.DeckLists;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchTest {

    /** Always makes player one start, so the turn order of the tests is explicit. */
    private static Random playerOneStarts() {
        return new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }
        };
    }

    private static List<Card> deck(List<Card> firstCards) {
        List<Card> cards = new ArrayList<>(firstCards);
        cards.addAll(Collections.nCopies(20 - firstCards.size(), CardCatalog.PIKACHU));
        return cards;
    }

    private static Match match(Hero hero1, List<Card> deck1, Hero hero2, List<Card> deck2) {
        return new Match(hero1, new Deck(deck(deck1)), hero2, new Deck(deck(deck2)), playerOneStarts());
    }

    private static Match match(List<Card> deck1, List<Card> deck2) {
        return match(HeroCatalog.ZAPDOS, deck1, HeroCatalog.ARTICUNO, deck2);
    }

    /**
     * Plays empty turns until the given global turn number, leaving the match in the PLAY phase.
     * Mana grows on each Hero's own turn, so player one has {@code (turn + 1) / 2} mana on its
     * odd turns and player two has {@code turn / 2} mana on its even turns.
     */
    private static void advanceToTurn(Match match, int turnNumber) {
        while (match.turnNumber() < turnNumber - 1) {
            match.beginTurn();
            match.endTurn();
        }
        match.beginTurn();
        assertEquals(turnNumber, match.turnNumber());
    }

    @Test
    void startingStateFollowsTheDesign() {
        Match match = match(List.of(), List.of());

        assertEquals(30, match.player1().currentHp());
        assertEquals(30, match.player2().currentHp());
        assertEquals(0, match.player1().maxMana());
        assertEquals(3, match.player1().hand().size());
        assertEquals(4, match.player2().hand().size());
        assertSame(match.player1(), match.firstPlayer());
        assertTrue(match.player1().board().isEmpty());
        assertTrue(match.player1().discard().isEmpty());
        assertSame(match.player1(), match.active());
        assertEquals(0, match.turnNumber());
    }

    @Test
    void theHeroPlayingSecondDrawsOneExtraCard() {
        Random playerTwoStarts = new Random() {
            @Override
            public boolean nextBoolean() {
                return false;
            }
        };
        Match match = new Match(HeroCatalog.ZAPDOS, new Deck(deck(List.of())),
                HeroCatalog.ARTICUNO, new Deck(deck(List.of())), playerTwoStarts);

        assertSame(match.player2(), match.firstPlayer());
        assertEquals(3, match.player2().hand().size());
        assertEquals(4, match.player1().hand().size());
    }

    @Test
    void aFullMatchPlayedWithThePredefinedDecksAlwaysTerminates() {
        Match match = new Match(HeroCatalog.MOLTRES, Deck.shuffled(DeckLists.cards(DeckId.AGGRO), new Random(11)),
                HeroCatalog.LUGIA, Deck.shuffled(DeckLists.cards(DeckId.CONTROL), new Random(12)), new Random(13));

        while (!match.isOver()) {
            match.beginTurn();
            if (!match.isOver()) {
                match.endTurn();
            }
        }

        assertTrue(match.result().turns() <= Match.MAX_TURNS);
    }

    @Test
    void aTurnRunsDrawThenManaThenPlay() {
        Match match = match(List.of(), List.of());

        match.beginTurn();

        assertEquals(1, match.turnNumber());
        assertEquals(Phase.PLAY, match.phase());
        assertEquals(4, match.player1().hand().size());
        assertEquals(1, match.player1().maxMana());
        assertEquals(1, match.player1().availableMana());
    }

    @Test
    void turnsAlternateBetweenTheTwoHeroes() {
        Match match = match(List.of(), List.of());

        match.beginTurn();
        match.endTurn();
        assertSame(match.player2(), match.active());
        assertEquals(Phase.END, match.phase());

        match.beginTurn();
        assertEquals(2, match.turnNumber());
        assertSame(match.player2(), match.active());
    }

    @Test
    void actionsAreOnlyAllowedDuringThePlayPhase() {
        Match match = match(List.of(), List.of());

        assertThrows(IllegalStateException.class, () -> match.perform(new Action.EndTurn()));
    }

    @Test
    void playingAPokemonSpendsManaAndSummonsItWithSummoningSickness() {
        Match match = match(List.of(CardCatalog.PIKACHU), List.of());
        match.beginTurn();

        match.perform(new Action.PlayCard(0));

        assertEquals(1, match.player1().board().size());
        assertEquals("pikachu", match.player1().board().at(0).id());
        assertEquals(0, match.player1().availableMana());
        assertThrows(IllegalActionException.class, () -> match.perform(Action.Attack.onHero(0)));
    }

    @Test
    void aPokemonCanAttackTheOpposingHeroOnTheFollowingTurn() {
        Match match = match(List.of(CardCatalog.PIKACHU), List.of());
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();
        match.endTurn();
        match.beginTurn();

        match.perform(Action.Attack.onHero(0));

        assertEquals(28, match.player2().currentHp());
        assertEquals(2, match.player1().damageDealtToOpposingHero());
        assertThrows(IllegalActionException.class, () -> match.perform(Action.Attack.onHero(0)));
    }

    @Test
    void pokemonFightingEachOtherDamageEachOtherSimultaneously() {
        Match match = match(List.of(CardCatalog.PIKACHU), List.of(CardCatalog.SQUIRTLE));
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        advanceToTurn(match, 4);
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();

        match.perform(new Action.Attack(0, 0));

        assertEquals(3, match.player2().board().at(0).currentHp());
        assertEquals(1, match.player1().board().at(0).currentHp());
        assertEquals(30, match.player2().currentHp());
    }

    @Test
    void aDeadPokemonLeavesTheBoardAndGoesToItsOwnersDiscardPile() {
        Match match = match(List.of(CardCatalog.PIKACHU), List.of(CardCatalog.PIKACHU));
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();

        match.perform(new Action.Attack(0, 0));

        assertTrue(match.player1().board().isEmpty());
        assertTrue(match.player2().board().isEmpty());
        assertEquals(List.of(CardCatalog.PIKACHU), match.player1().discard());
        assertEquals(List.of(CardCatalog.PIKACHU), match.player2().discard());
    }

    @Test
    void aFourthPokemonCannotBePlayedAndTheCardIsKept() {
        Match match = match(List.of(CardCatalog.PIKACHU, CardCatalog.PIKACHU, CardCatalog.PIKACHU), List.of());
        advanceToTurn(match, 5);

        match.perform(new Action.PlayCard(0));
        match.perform(new Action.PlayCard(0));
        match.perform(new Action.PlayCard(0));
        int handSize = match.player1().hand().size();
        int mana = match.player1().availableMana();

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0)));
        assertEquals(3, match.player1().board().size());
        assertEquals(handSize, match.player1().hand().size());
        assertEquals(mana, match.player1().availableMana());
    }

    @Test
    void anItemResolvesImmediatelyAndGoesToTheDiscardPile() {
        Match match = match(List.of(CardCatalog.POTION), List.of());
        match.player1().receiveDamage(10);
        advanceToTurn(match, 3);

        match.perform(new Action.PlayCard(0));

        assertEquals(25, match.player1().currentHp());
        assertEquals(List.of(CardCatalog.POTION), match.player1().discard());
        assertEquals(0, match.player1().availableMana());
    }

    @Test
    void rockThrowIsIllegalWithoutAnOpposingPokemonAndIsNotConsumed() {
        Match match = match(List.of(CardCatalog.ROCK_THROW), List.of());
        advanceToTurn(match, 3);

        assertThrows(IllegalActionException.class, () -> match.perform(new Action.PlayCard(0)));

        assertEquals(CardCatalog.ROCK_THROW, match.player1().hand().get(0));
        assertTrue(match.player1().discard().isEmpty());
        assertEquals(2, match.player1().availableMana());
    }

    @Test
    void rockThrowDamagesTheTargetedOpposingPokemon() {
        Match match = match(List.of(CardCatalog.ROCK_THROW), List.of(CardCatalog.SQUIRTLE));
        advanceToTurn(match, 4);
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();

        match.perform(new Action.PlayCard(0, 0));

        assertEquals(2, match.player2().board().at(0).currentHp());
        assertEquals(List.of(CardCatalog.ROCK_THROW), match.player1().discard());
    }

    @Test
    void theHeroPowerCanOnlyBeUsedOncePerTurn() {
        Match match = match(List.of(), List.of());
        advanceToTurn(match, 5);

        match.perform(new Action.UseHeroPower());

        assertEquals(28, match.player2().currentHp());
        assertEquals(1, match.player1().availableMana());
        assertThrows(IllegalActionException.class, () -> match.perform(new Action.UseHeroPower()));

        match.endTurn();
        match.beginTurn();
        match.endTurn();
        match.beginTurn();
        match.perform(new Action.UseHeroPower());

        assertEquals(26, match.player2().currentHp());
    }

    @Test
    void attackBuffsStackAndOnlyApplyToAttackEffects() {
        Match match = match(List.of(CardCatalog.ABRA), List.of());
        advanceToTurn(match, 9);

        match.perform(new Action.PlayCard(0));
        match.perform(new Action.UseHeroPower());

        // Static Charge deals 2, boosted by Abra's +3.
        assertEquals(25, match.player2().currentHp());
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void pokemonCombatDamageIsNotBoostedByAttackBuffsAndPidgeyBuffExpiresAtEndOfTurn() {
        Match match = match(List.of(CardCatalog.PIKACHU, CardCatalog.PIDGEY), List.of());
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        advanceToTurn(match, 3);

        match.perform(new Action.PlayCard(0));
        match.perform(Action.Attack.onHero(0));

        assertEquals(28, match.player2().currentHp());
        assertEquals(1, match.player1().temporaryEffects().size());

        match.endTurn();
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void onixDamageReductionBelongsToTheHeroAndSurvivesItsDeath() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(CardCatalog.ONIX), HeroCatalog.ZAPDOS, List.of());
        advanceToTurn(match, 5);

        match.perform(new Action.PlayCard(0));
        match.player1().board().at(0).receiveDamage(99);
        match.endTurn();

        assertTrue(match.player1().board().isEmpty());
        assertEquals(List.of(CardCatalog.ONIX), match.player1().discard());
        assertEquals(1, match.player1().temporaryEffects().size());

        match.beginTurn();
        match.perform(new Action.UseHeroPower());

        assertEquals(30, match.player1().currentHp());
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void frozenBarrierExpiresAtTheEndOfTheFollowingTurn() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(), HeroCatalog.ZAPDOS, List.of());
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());
        assertEquals(1, match.player1().temporaryEffects().size());

        match.endTurn();
        assertEquals(1, match.player1().temporaryEffects().size());

        match.beginTurn();
        match.endTurn();
        assertTrue(match.player1().temporaryEffects().isEmpty());
    }

    @Test
    void theMatchEndsImmediatelyWhenAHeroReachesZeroHp() {
        Match match = match(List.of(), List.of());
        match.player2().receiveDamage(29);
        advanceToTurn(match, 3);

        match.perform(new Action.UseHeroPower());

        assertTrue(match.isOver());
        assertEquals(MatchResult.Outcome.PLAYER_ONE_WINS, match.result().outcome());
        assertEquals(3, match.result().turns());
        assertFalse(match.player2().isAlive());
        assertThrows(IllegalStateException.class, match::beginTurn);
    }

    @Test
    void anIdleMatchStopsAfterFiftyTurnsAndIsADraw() {
        Match match = match(List.of(), List.of());

        while (!match.isOver()) {
            match.beginTurn();
            match.endTurn();
        }

        assertEquals(50, match.result().turns());
        assertTrue(match.result().isDraw());
        assertTrue(match.result().reason().contains("turn limit"), match.result().reason());
    }

    @Test
    void theHeroWithTheHighestHpWinsWhenTheTurnLimitIsReached() {
        Match match = match(List.of(), List.of());
        match.player2().receiveDamage(5);

        while (!match.isOver()) {
            match.beginTurn();
            match.endTurn();
        }

        assertEquals(MatchResult.Outcome.PLAYER_ONE_WINS, match.result().outcome());
        assertEquals(50, match.result().turns());
    }
}
