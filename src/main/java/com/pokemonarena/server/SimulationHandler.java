package com.pokemonarena.server;

import com.pokemonarena.bot.Bots;
import com.pokemonarena.simulation.Simulation;
import com.pokemonarena.simulation.SimulationConfig;
import com.pokemonarena.simulation.SimulationStats;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@code POST /api/simulations}: runs an AI vs AI batch with {@link Simulation} and returns the
 * statistics plus the events of the first match, so it can be replayed.
 */
final class SimulationHandler implements HttpHandler {

    /** Keeps a single request from blocking the server for too long. */
    static final int MAX_MATCHES = 10_000;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            Http.sendError(exchange, 405, "use POST");
            return;
        }
        Map<String, Object> body = Http.readJson(exchange);
        Map<String, Object> p1 = JsonMapper.object(body.get("p1"));
        Map<String, Object> p2 = JsonMapper.object(body.get("p2"));
        int matches = JsonMapper.integer(body, "matches");
        if (matches > MAX_MATCHES) {
            throw new IllegalArgumentException("at most " + MAX_MATCHES + " matches per simulation");
        }
        Long requestedSeed = JsonMapper.optionalLong(body, "seed");
        long seed = requestedSeed != null ? requestedSeed : ThreadLocalRandom.current().nextLong();

        SimulationConfig config = new SimulationConfig(
                JsonMapper.toSetup(p1, "hero", "deck"), Bots.byName(JsonMapper.string(p1, "bot")),
                JsonMapper.toSetup(p2, "hero", "deck"), Bots.byName(JsonMapper.string(p2, "bot")),
                seed, matches);
        EventBuffer firstMatch = new EventBuffer();
        SimulationStats stats = Simulation.run(config, firstMatch);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("seed", String.valueOf(seed));
        json.put("stats", JsonMapper.stats(stats));
        json.put("firstMatchEvents", JsonMapper.events(firstMatch.drain()));
        Http.sendJson(exchange, 200, json);
    }
}
