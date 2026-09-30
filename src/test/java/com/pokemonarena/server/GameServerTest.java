package com.pokemonarena.server;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starts the real server on a free port and drives it over HTTP. */
class GameServerTest {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String NEW_MATCH = """
            {"humanHero":"zapdos","humanDeck":"AGGRO","aiHero":"articuno","aiDeck":"CONTROL",
             "aiBot":"Defensive","seed":%s}""";

    private static HttpServer server;
    private static String base;

    record Reply(int status, Map<String, Object> body, String raw) {
    }

    @BeforeAll
    static void start() throws IOException {
        server = GameServer.start(0, Path.of("frontend"));
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    private static Reply call(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path));
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> response = CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
        Map<String, Object> json = response.headers().firstValue("Content-Type").orElse("").startsWith("application/json")
                ? JsonMapper.object(Json.parse(response.body())) : Map.of();
        return new Reply(response.statusCode(), json, response.body());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private static List<Map<String, Object>> list(Object value) {
        return ((List<?>) value).stream().map(GameServerTest::map).toList();
    }

    @Test
    void servesTheFrontend() throws Exception {
        Reply index = call("GET", "/", null);
        assertEquals(200, index.status());
        assertTrue(index.raw().contains("<html"));
        assertEquals(404, call("GET", "/../pom.xml", null).status());
        assertEquals(404, call("GET", "/nope.js", null).status());
    }

    @Test
    void catalogEndpoint() throws Exception {
        Reply reply = call("GET", "/api/catalog", null);
        assertEquals(200, reply.status());
        assertEquals(25, ((List<?>) reply.body().get("cards")).size());
        assertEquals(5, ((List<?>) reply.body().get("heroes")).size());
        assertEquals(4, ((List<?>) reply.body().get("decks")).size());
    }

    @Test
    void createMatchReturnsTheHumanViewWithEvents() throws Exception {
        Reply created = call("POST", "/api/matches", NEW_MATCH.formatted(7));
        assertEquals(201, created.status());
        Map<String, Object> view = map(created.body().get("view"));
        assertEquals("zapdos", map(view.get("self")).get("heroId"));
        assertEquals(1L, map(view.get("self")).get("side"));
        assertEquals("articuno", map(view.get("opponent")).get("heroId"));
        assertEquals(1L, view.get("activeSide"), "the AI turn is already played when the human is waiting");
        assertFalse(((List<?>) view.get("legalActions")).isEmpty());
        assertEquals("MatchStarted", list(created.body().get("events")).get(0).get("type"));

        Reply again = call("GET", "/api/matches/" + created.body().get("matchId"), null);
        assertEquals(200, again.status());
        assertTrue(((List<?>) again.body().get("events")).isEmpty(), "events are delivered once");
        assertEquals(view, again.body().get("view"));
    }

    @Test
    void legalActionIsExecutedAndTheAiPlaysItsTurn() throws Exception {
        Reply created = call("POST", "/api/matches", NEW_MATCH.formatted(11));
        String id = (String) created.body().get("matchId");
        long turn = (long) map(created.body().get("view")).get("turnNumber");

        Map<String, Object> firstLegal = list(map(created.body().get("view")).get("legalActions")).stream()
                .filter(a -> !a.get("type").equals("EndTurn")).findFirst().orElse(Map.of("type", "EndTurn"));
        Reply played = call("POST", "/api/matches/" + id + "/actions", Json.write(firstLegal));
        assertEquals(200, played.status(), played.raw());
        assertFalse(((List<?>) played.body().get("events")).isEmpty());

        Reply ended = call("POST", "/api/matches/" + id + "/actions", "{\"type\":\"EndTurn\"}");
        assertEquals(200, ended.status(), ended.raw());
        Map<String, Object> view = map(ended.body().get("view"));
        List<Map<String, Object>> events = list(ended.body().get("events"));
        if (!(boolean) view.get("over")) {
            assertEquals(turn + 2, view.get("turnNumber"), "the AI turn was played automatically");
            assertEquals(1L, view.get("activeSide"));
            assertTrue(events.stream().anyMatch(e -> e.get("type").equals("TurnStarted") && e.get("player").equals(2L)));
        }
    }

    @Test
    void illegalActionsAreRejectedWithoutChangingTheMatch() throws Exception {
        Reply created = call("POST", "/api/matches", NEW_MATCH.formatted(5));
        String id = (String) created.body().get("matchId");

        Reply bad = call("POST", "/api/matches/" + id + "/actions", "{\"type\":\"PlayCard\",\"handIndex\":99}");
        assertEquals(400, bad.status());
        assertTrue(bad.body().containsKey("error"));
        assertEquals(400, call("POST", "/api/matches/" + id + "/actions", "{\"type\":\"Nope\"}").status());
        assertEquals(400, call("POST", "/api/matches/" + id + "/actions", "not json").status());
        assertEquals(404, call("POST", "/api/matches/unknown/actions", "{\"type\":\"EndTurn\"}").status());
        assertEquals(400, call("POST", "/api/matches", "{\"humanHero\":\"mew\"}").status());

        assertEquals(created.body().get("view"), call("GET", "/api/matches/" + id, null).body().get("view"));
    }

    @Test
    void aSeededMatchIsReproducible() throws Exception {
        Reply a = call("POST", "/api/matches", NEW_MATCH.formatted(42));
        Reply b = call("POST", "/api/matches", NEW_MATCH.formatted(42));
        assertNotEquals(a.body().get("matchId"), b.body().get("matchId"));
        assertEquals("42", a.body().get("seed"));
        assertEquals(a.body().get("view"), b.body().get("view"));
        assertEquals(a.body().get("events"), b.body().get("events"));

        String end = "{\"type\":\"EndTurn\"}";
        Reply a2 = call("POST", "/api/matches/" + a.body().get("matchId") + "/actions", end);
        Reply b2 = call("POST", "/api/matches/" + b.body().get("matchId") + "/actions", end);
        assertEquals(a2.body().get("view"), b2.body().get("view"));
        assertEquals(a2.body().get("events"), b2.body().get("events"));

        Reply unseeded = call("POST", "/api/matches", NEW_MATCH.formatted("null"));
        assertEquals(201, unseeded.status());
        assertFalse(((String) unseeded.body().get("seed")).isEmpty());
    }

    @Test
    void simulationEndpoint() throws Exception {
        String body = """
                {"p1":{"hero":"zapdos","deck":"AGGRO","bot":"Aggressive"},
                 "p2":{"hero":"articuno","deck":"CONTROL","bot":"Defensive"},"matches":20,"seed":"3"}""";
        Reply a = call("POST", "/api/simulations", body);
        assertEquals(200, a.status(), a.raw());
        Map<String, Object> stats = map(a.body().get("stats"));
        assertEquals(20L, stats.get("matches"));
        assertEquals(20L, (long) stats.get("winsSide1") + (long) stats.get("winsSide2") + (long) stats.get("draws"));
        List<Map<String, Object>> events = list(a.body().get("firstMatchEvents"));
        assertEquals("MatchStarted", events.get(0).get("type"));
        assertEquals("MatchEnded", events.get(events.size() - 1).get("type"));
        assertEquals(1, events.stream().filter(e -> e.get("type").equals("MatchStarted")).count());

        assertEquals(a.body(), call("POST", "/api/simulations", body).body(), "same seed, same result");
        assertEquals(400, call("POST", "/api/simulations", body.replace("\"matches\":20", "\"matches\":0")).status());
    }
}
