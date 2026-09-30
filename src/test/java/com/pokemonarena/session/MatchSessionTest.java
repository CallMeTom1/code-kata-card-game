package com.pokemonarena.session;

import com.pokemonarena.bot.AggressiveBot;
import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.bot.DefensiveBot;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.Match;
import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.game.MatchListener;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroCatalog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchSessionTest {

    @Test
    void everyHeroAndDeckCombinationCanBePlayedByBothBotsAgainstEachOther() {
        List<BotStrategy> bots = List.of(new AggressiveBot(), new DefensiveBot());
        long seed = 0;
        for (Hero hero : HeroCatalog.all()) {
            for (DeckId deck : DeckId.values()) {
                for (BotStrategy bot : bots) {
                    BotStrategy other = bot instanceof AggressiveBot ? new DefensiveBot() : new AggressiveBot();
                    MatchSession session = MatchSession.aiVsAi(new PlayerSetup(hero, deck), bot,
                            new PlayerSetup(HeroCatalog.ZAPDOS, DeckId.BALANCED), other, seed++, MatchListener.NONE);
                    var result = session.playToEnd();
                    assertTrue(session.isOver());
                    assertTrue(result.turns() >= 1 && result.turns() <= Match.MAX_TURNS);
                }
            }
        }
    }

    @Test
    void sameSeedProducesTheSameSequenceOfEvents() {
        assertEquals(events(7), events(7));
    }

    private static List<MatchEvent> events(long seed) {
        List<MatchEvent> events = new ArrayList<>();
        MatchSession.aiVsAi(new PlayerSetup(HeroCatalog.MOLTRES, DeckId.AGGRO), new AggressiveBot(),
                new PlayerSetup(HeroCatalog.ARTICUNO, DeckId.CONTROL), new DefensiveBot(), seed, events::add)
                .playToEnd();
        return events;
    }

    @Test
    void humanVsAiWaitsForTheHumanAndLetsTheBotPlayItsTurns() {
        List<MatchEvent> events = new ArrayList<>();
        MatchSession session = MatchSession.humanVsAi(new PlayerSetup(HeroCatalog.LUGIA, DeckId.BALANCED),
                new PlayerSetup(HeroCatalog.MOLTRES, DeckId.AGGRO), new AggressiveBot(), 3, events::add);

        session.advance();
        while (!session.isOver()) {
            assertTrue(session.isAwaitingHuman());
            Match match = session.match();
            assertEquals(match.player1(), match.active());
            assertTrue(session.view().legalActions().contains(new Action.EndTurn()));
            session.submit(new Action.EndTurn());   // a passive human
        }

        assertTrue(session.result().isPresent());
        assertFalse(session.isAwaitingHuman());
        assertTrue(events.stream().anyMatch(e -> e instanceof MatchEvent.CardPlayed played && played.player() == 2),
                "the AI must have played cards on its own");
    }

    @Test
    void humanActionsAreRejectedWhenItIsNotTheHumansTurn() {
        MatchSession session = MatchSession.humanVsAi(new PlayerSetup(HeroCatalog.LUGIA, DeckId.BALANCED),
                new PlayerSetup(HeroCatalog.MOLTRES, DeckId.AGGRO), new AggressiveBot(), 3, MatchListener.NONE);

        assertThrows(IllegalStateException.class, () -> session.submit(new Action.EndTurn()));
        assertThrows(IllegalStateException.class, session::playToEnd);
    }
}
