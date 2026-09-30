package com.pokemonarena.simulation;

import com.pokemonarena.game.MatchListener;
import com.pokemonarena.session.MatchSession;

/**
 * Runs N AI vs AI matches with the same engine as every other mode. It does not depend on
 * any frontend; an optional listener observes the first match (e.g. the text log).
 */
public final class Simulation {

    private Simulation() {
    }

    public static SimulationStats run(SimulationConfig config) {
        return run(config, MatchListener.NONE);
    }

    public static SimulationStats run(SimulationConfig config, MatchListener firstMatchListener) {
        SimulationStats stats = new SimulationStats();
        for (int i = 0; i < config.matches(); i++) {
            MatchListener listener = i == 0 ? firstMatchListener : MatchListener.NONE;
            MatchSession session = MatchSession.aiVsAi(config.side1(), config.bot1(), config.side2(), config.bot2(),
                    config.seedOf(i), listener);
            session.playToEnd();
            stats.record(session.match());
        }
        return stats;
    }
}
