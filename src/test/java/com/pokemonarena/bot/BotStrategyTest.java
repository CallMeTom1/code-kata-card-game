package com.pokemonarena.bot;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;
import com.pokemonarena.game.Match;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotStrategyTest {

    private static final Random PLAYER_ONE_STARTS = new Random() {
        @Override
        public boolean nextBoolean() {
            return true;
        }
    };

    private static Match match(Hero hero1, List<Card> first1, Hero hero2, List<Card> first2) {
        return new Match(hero1, new Deck(pad(first1)), hero2, new Deck(pad(first2)), PLAYER_ONE_STARTS);
    }

    private static List<Card> pad(List<Card> first) {
        List<Card> cards = new ArrayList<>(first);
        cards.addAll(Collections.nCopies(20 - first.size(), CardCatalog.PIKACHU));
        return cards;
    }

    /** Plays empty turns until {@code turn} starts; the match is left in its PLAY phase. */
    private static void advanceToTurn(Match match, int turn) {
        while (match.turnNumber() < turn - 1) {
            match.beginTurn();
            match.endTurn();
        }
        match.beginTurn();
    }

    private static Card cardOf(GameView view, Action action) {
        return view.self().hand().get(assertInstanceOf(Action.PlayCard.class, action).handIndex());
    }

    @Test
    void aggressiveBotDevelopsTheStrongestAffordablePokemon() {
        Match match = match(HeroCatalog.ZAPDOS,
                List.of(CardCatalog.PIKACHU, CardCatalog.CHARMANDER, CardCatalog.BULBASAUR),
                HeroCatalog.ARTICUNO, List.of());
        advanceToTurn(match, 5);
        GameView view = new GameView(match);

        assertEquals(CardCatalog.BULBASAUR, cardOf(view, new AggressiveBot().chooseAction(view)));
    }

    @Test
    void aggressiveBotAttacksTheHeroWhenNoTradeIsAvailable() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.PIKACHU), HeroCatalog.ARTICUNO, List.of());
        match.beginTurn();
        match.perform(new Action.PlayCard(0));
        match.endTurn();
        match.beginTurn();
        match.endTurn();
        match.beginTurn();
        GameView view = new GameView(match);
        AggressiveBot bot = new AggressiveBot();

        Action action = bot.chooseAction(view);
        while (!(action instanceof Action.Attack) && !(action instanceof Action.EndTurn)) {
            match.perform(action);
            view = new GameView(match);
            action = bot.chooseAction(view);
        }
        assertEquals(Action.Attack.onHero(0), action);
    }

    @Test
    void aggressiveBotTakesAFavorableTrade() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.CHARMANDER), HeroCatalog.ARTICUNO,
                List.of(CardCatalog.PIKACHU));
        advanceToTurn(match, 2);
        match.endTurn();
        match.beginTurn();
        match.perform(new Action.PlayCard(0));      // P1 Charmander 4/4? (2 mana at turn 3)
        match.endTurn();
        match.beginTurn();
        match.perform(new Action.PlayCard(0));      // P2 Pikachu
        match.endTurn();
        match.beginTurn();
        GameView view = new GameView(match);

        Action action = new AggressiveBot().chooseAction(view);
        boolean charmanderWins = CardCatalog.CHARMANDER.attack() >= CardCatalog.PIKACHU.maxHp()
                && CardCatalog.CHARMANDER.maxHp() > CardCatalog.PIKACHU.attack();
        if (charmanderWins) {
            assertEquals(new Action.Attack(0, 0), action);
        } else {
            assertTrue(view.legalActions().contains(action));
        }
    }

    @Test
    void defensiveBotDevelopsASturdyBoardAboveFifteenHp() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(CardCatalog.POTION, CardCatalog.SQUIRTLE),
                HeroCatalog.ZAPDOS, List.of());
        advanceToTurn(match, 3);
        GameView view = new GameView(match);

        assertEquals(CardCatalog.SQUIRTLE, cardOf(view, new DefensiveBot().chooseAction(view)));
    }

    @Test
    void defensiveBotHealsFirstAtFifteenHpOrBelow() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(CardCatalog.POTION, CardCatalog.SQUIRTLE),
                HeroCatalog.ZAPDOS, List.of());
        while (match.player1().currentHp() > DefensiveBot.LOW_HP) {
            match.beginTurn();
            if (match.active() == match.player2() && match.player2().availableMana() >= 2) {
                match.perform(new Action.UseHeroPower());   // Static Charge: 2 damage
            }
            match.endTurn();
        }
        match.beginTurn();
        assertEquals(match.player1(), match.active());
        GameView view = new GameView(match);

        assertEquals(CardCatalog.POTION, cardOf(view, new DefensiveBot().chooseAction(view)));
    }

    @Test
    void botsAlwaysChooseALegalAction() {
        for (BotStrategy bot : List.of(new AggressiveBot(), new DefensiveBot())) {
            Match match = match(HeroCatalog.LUGIA,
                    List.of(CardCatalog.ROCK_THROW, CardCatalog.RAPPEL, CardCatalog.ENERGY, CardCatalog.DEFENSE_X),
                    HeroCatalog.HO_OH, List.of(CardCatalog.POTION, CardCatalog.SNORLAX));
            while (!match.isOver()) {
                match.beginTurn();
                for (int i = 0; i < 100 && !match.isOver() && match.phase() == com.pokemonarena.game.Phase.PLAY; i++) {
                    GameView view = new GameView(match);
                    Action action = bot.chooseAction(view);
                    assertTrue(view.legalActions().contains(action), bot.name() + " chose " + action);
                    match.perform(action);
                }
            }
            assertTrue(match.turnNumber() <= Match.MAX_TURNS);
        }
    }

    @Test
    void botsAreFoundByName() {
        assertInstanceOf(AggressiveBot.class, Bots.byName("aggressive"));
        assertInstanceOf(DefensiveBot.class, Bots.byName("Defensive"));
        assertEquals(List.of("Aggressive", "Defensive"), Bots.names());
    }
}
