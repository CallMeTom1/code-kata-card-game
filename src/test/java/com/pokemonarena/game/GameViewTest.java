package com.pokemonarena.game;

import com.pokemonarena.bot.AggressiveBot;
import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.bot.DefensiveBot;
import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameViewTest {

    private static final Random PLAYER_ONE_STARTS = new Random() {
        @Override
        public boolean nextBoolean() {
            return true;
        }
    };

    private static Match matchWithPokemonOnBoard() {
        List<Card> cards = new ArrayList<>(List.of(CardCatalog.PIKACHU, CardCatalog.DEFENSE_X));
        cards.addAll(Collections.nCopies(18, CardCatalog.PIKACHU));
        Match match = new Match(HeroCatalog.ZAPDOS, new Deck(cards), HeroCatalog.ARTICUNO,
                new Deck(Collections.nCopies(20, CardCatalog.PIKACHU)), PLAYER_ONE_STARTS);
        match.beginTurn();
        match.perform(new Action.PlayCard(0));      // Pikachu on the Board
        match.endTurn();
        match.beginTurn();
        match.endTurn();
        match.beginTurn();                          // P1, turn 3, 2 mana
        match.perform(new Action.PlayCard(0));      // Defense X: queues a reduction, goes to the discard
        return match;
    }

    @Test
    void viewContainsEverythingADecisionMakerNeeds() {
        Match match = matchWithPokemonOnBoard();
        GameView view = new GameView(match);
        PlayerView self = view.self();

        assertEquals(1, view.activeSide());
        assertEquals(Phase.PLAY, view.phase());
        assertEquals(3, view.turnNumber());
        assertFalse(view.isOver());
        assertEquals(match.legalActions(), view.legalActions());
        assertEquals(HeroCatalog.ZAPDOS, self.hero());
        assertEquals(match.player1().currentHp(), self.currentHp());
        assertEquals(30, self.maxHp());
        assertEquals(match.player1().availableMana(), self.availableMana());
        assertEquals(2, self.maxMana());
        assertFalse(self.heroPowerUsedThisTurn());
        assertEquals(match.player1().hand(), self.hand());
        assertEquals(match.player1().deck().size(), self.deckCount());
        assertEquals(match.player1().discard(), self.discard());
        assertEquals(match.player1().temporaryEffects(), self.temporaryEffects());
        assertEquals(1, self.board().size());
        PokemonView pikachu = self.board().get(0);
        assertEquals("pikachu", pikachu.id());
        assertEquals(CardCatalog.PIKACHU.attack(), pikachu.attack());
        assertEquals(CardCatalog.PIKACHU.maxHp(), pikachu.currentHp());
        assertTrue(pikachu.canAttack());
        assertEquals(2, view.opponent().side());
    }

    @Test
    void viewCollectionsCannotBeModified() {
        GameView view = new GameView(matchWithPokemonOnBoard());
        PlayerView self = view.self();

        assertThrows(UnsupportedOperationException.class, () -> self.hand().clear());
        assertThrows(UnsupportedOperationException.class, () -> self.board().clear());
        assertThrows(UnsupportedOperationException.class, () -> self.discard().add(CardCatalog.PIKACHU));
        assertThrows(UnsupportedOperationException.class, () -> self.temporaryEffects().clear());
        assertThrows(UnsupportedOperationException.class, () -> view.legalActions().clear());
    }

    @Test
    void viewIsASnapshotDetachedFromTheLiveMatch() {
        Match match = matchWithPokemonOnBoard();
        GameView view = new GameView(match);
        int hpBefore = view.opponent().currentHp();

        match.perform(Action.Attack.onHero(0));

        assertEquals(hpBefore, view.opponent().currentHp());
        assertTrue(view.self().board().get(0).canAttack());
        assertTrue(match.player2().currentHp() < hpBefore);
        assertFalse(new GameView(match).self().board().get(0).canAttack());
    }

    @Test
    void forSideAlwaysShowsThatSideAsSelf() {
        Match match = matchWithPokemonOnBoard();          // side 1 is active

        GameView human = GameView.forSide(match, 2);

        assertEquals(2, human.self().side());
        assertEquals(HeroCatalog.ARTICUNO, human.self().hero());
        assertEquals(match.player2().hand(), human.self().hand());
        assertEquals(1, human.opponent().side());
        assertEquals(HeroCatalog.ZAPDOS, human.opponent().hero());
        assertEquals(1, human.opponent().board().size());
        assertEquals(match.player1().currentHp(), human.opponent().currentHp());
        assertEquals(1, human.activeSide());
    }

    @Test
    void forSideHasLegalActionsOnlyWhenThatSideCanAct() {
        Match match = matchWithPokemonOnBoard();

        assertTrue(GameView.forSide(match, 2).legalActions().isEmpty());
        assertEquals(match.legalActions(), GameView.forSide(match, 1).legalActions());

        match.endTurn();
        assertTrue(GameView.forSide(match, 1).legalActions().isEmpty());
        assertTrue(GameView.forSide(match, 2).legalActions().isEmpty(), "no action outside PLAY");
        match.beginTurn();
        assertFalse(GameView.forSide(match, 2).legalActions().isEmpty());
        assertTrue(GameView.forSide(match, 1).legalActions().isEmpty());
    }

    @Test
    void forSideViewIsImmutableAndDetached() {
        Match match = matchWithPokemonOnBoard();
        GameView human = GameView.forSide(match, 2);
        int hpBefore = human.self().currentHp();

        assertThrows(UnsupportedOperationException.class, () -> human.self().hand().clear());
        assertThrows(UnsupportedOperationException.class, () -> human.opponent().board().clear());
        assertThrows(UnsupportedOperationException.class, () -> human.legalActions().clear());
        match.perform(Action.Attack.onHero(0));
        assertEquals(hpBefore, human.self().currentHp());
        assertThrows(IllegalArgumentException.class, () -> GameView.forSide(match, 3));
    }

    @Test
    void botsCannotMutateTheMatchThroughTheView() {
        for (BotStrategy bot : List.of(new AggressiveBot(), new DefensiveBot())) {
            Match match = matchWithPokemonOnBoard();
            String before = match.player1() + " | " + match.player2() + " | "
                    + match.player1().temporaryEffects() + " | " + match.player1().discard();

            bot.chooseAction(new GameView(match));

            String after = match.player1() + " | " + match.player2() + " | "
                    + match.player1().temporaryEffects() + " | " + match.player1().discard();
            assertEquals(before, after, bot.name());
        }
    }
}
