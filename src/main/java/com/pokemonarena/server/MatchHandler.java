package com.pokemonarena.server;

import com.pokemonarena.bot.Bots;
import com.pokemonarena.game.GameView;
import com.pokemonarena.session.MatchSession;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Human vs AI endpoints. Thin adapter: it builds sessions, converts JSON to {@code Action}s and
 * returns the human's {@link GameView} plus the new events; the engine validates everything.
 * <ul>
 *     <li>{@code POST /api/matches}</li>
 *     <li>{@code GET /api/matches/{id}}</li>
 *     <li>{@code POST /api/matches/{id}/actions}</li>
 * </ul>
 */
final class MatchHandler implements HttpHandler {

    private final MatchRegistry registry;

    MatchHandler(MatchRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().replaceAll("/+$", "").split("/");
        // "", "api", "matches", [id], ["actions"]
        String method = exchange.getRequestMethod();
        if (parts.length == 3 && method.equals("POST")) {
            create(exchange);
        } else if (parts.length == 4 && method.equals("GET")) {
            MatchRegistry.Entry entry = entry(parts[3]);
            synchronized (entry) {
                Http.sendJson(exchange, 200, response(entry));
            }
        } else if (parts.length == 5 && parts[4].equals("actions") && method.equals("POST")) {
            MatchRegistry.Entry entry = entry(parts[3]);
            var action = JsonMapper.toAction(Http.readJson(exchange));
            synchronized (entry) {
                entry.session().submit(action);
                Http.sendJson(exchange, 200, response(entry));
            }
        } else {
            Http.sendError(exchange, 404, "no such endpoint: " + method + " " + exchange.getRequestURI().getPath());
        }
    }

    private void create(HttpExchange exchange) throws IOException {
        Map<String, Object> body = Http.readJson(exchange);
        Long requestedSeed = JsonMapper.optionalLong(body, "seed");
        long seed = requestedSeed != null ? requestedSeed : ThreadLocalRandom.current().nextLong();
        MatchRegistry.Entry entry = registry.create(
                JsonMapper.toSetup(body, "humanHero", "humanDeck"),
                JsonMapper.toSetup(body, "aiHero", "aiDeck"),
                Bots.byName(JsonMapper.string(body, "aiBot")),
                seed);
        synchronized (entry) {
            entry.session().advance();
            Http.sendJson(exchange, 201, response(entry));
        }
    }

    private MatchRegistry.Entry entry(String id) {
        return registry.find(id).orElseThrow(() -> new UnknownMatchException(id));
    }

    static Map<String, Object> response(MatchRegistry.Entry entry) {
        MatchSession session = entry.session();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("matchId", entry.id());
        json.put("seed", String.valueOf(entry.seed()));
        json.put("humanSide", MatchRegistry.HUMAN_SIDE);
        json.put("view", JsonMapper.view(GameView.forSide(session.match(), MatchRegistry.HUMAN_SIDE)));
        json.put("result", JsonMapper.result(session.result().orElse(null)));
        json.put("events", JsonMapper.events(entry.events().drain()));
        return json;
    }

    /** Thrown for an unknown match id, mapped to HTTP 404. */
    static final class UnknownMatchException extends RuntimeException {
        UnknownMatchException(String id) {
            super("unknown match: " + id);
        }
    }
}
