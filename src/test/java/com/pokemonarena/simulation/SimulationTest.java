package com.pokemonarena.simulation;

import com.pokemonarena.bot.AggressiveBot;
import com.pokemonarena.bot.DefensiveBot;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.game.Match;
import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.hero.HeroCatalog;
import com.pokemonarena.logging.MatchLogger;
import com.pokemonarena.session.PlayerSetup;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static SimulationConfig config(long seed, int matches) {
        return new SimulationConfig(new PlayerSetup(HeroCatalog.ZAPDOS, DeckId.AGGRO), new AggressiveBot(),
                new PlayerSetup(HeroCatalog.ARTICUNO, DeckId.CONTROL), new DefensiveBot(), seed, matches);
    }

    @Test
    void nMatchesTerminateAndAreAllCounted() {
        SimulationStats stats = Simulation.run(config(1, 50));

        assertEquals(50, stats.matches());
        assertEquals(50, stats.winsSide1() + stats.winsSide2() + stats.draws());
        assertEquals(1.0, stats.winRateSide1() + stats.winRateSide2() + stats.drawRate(), 1e-9);
        assertTrue(stats.averageTurns() >= 1 && stats.averageTurns() <= Match.MAX_TURNS);
        assertTrue(stats.averageDamageSide1() >= 0 && stats.averageDamageSide2() >= 0);
    }

    @Test
    void aggregateStatisticsMatchTheIndividualMatches() {
        SimulationStats single = Simulation.run(config(10, 1));
        SimulationStats pair = Simulation.run(config(10, 2));
        SimulationStats second = Simulation.run(config(11, 1));

        assertEquals((single.averageTurns() + second.averageTurns()) / 2, pair.averageTurns(), 1e-9);
        assertEquals((single.averageDamageSide1() + second.averageDamageSide1()) / 2, pair.averageDamageSide1(), 1e-9);
        assertEquals(single.winsSide1() + second.winsSide1(), pair.winsSide1());
        assertEquals(single.draws() + second.draws(), pair.draws());
    }

    @Test
    void aFixedSeedGivesDeterministicResults() {
        SimulationStats a = Simulation.run(config(42, 30));
        SimulationStats b = Simulation.run(config(42, 30));

        assertEquals(a.winsSide1(), b.winsSide1());
        assertEquals(a.winsSide2(), b.winsSide2());
        assertEquals(a.draws(), b.draws());
        assertEquals(a.averageTurns(), b.averageTurns());
        assertEquals(a.averageDamageSide1(), b.averageDamageSide1());
        assertEquals(a.averageDamageSide2(), b.averageDamageSide2());
    }

    @Test
    void onlyTheFirstMatchIsObservedAndLogged() {
        int[] matchStarts = {0};
        Simulation.run(config(5, 3), event -> {
            if (event instanceof MatchEvent.MatchStarted) {
                matchStarts[0]++;
            }
        });
        assertEquals(1, matchStarts[0]);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Simulation.run(config(5, 1), new MatchLogger(new PrintStream(buffer, true, StandardCharsets.UTF_8)));
        String log = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("starting hand"));
        assertTrue(log.contains("=== Turn 1"));
        assertTrue(log.contains("Draw: "));
        assertTrue(log.contains("=== Result after"));
    }

    @Test
    void aSimulationNeedsAtLeastOneMatch() {
        assertThrows(IllegalArgumentException.class, () -> config(1, 0));
    }
}
