package com.arena.server;

import com.arena.cli.MatchFactory;
import com.arena.cli.PlayerSpec;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.match.Contender;
import com.arena.engine.match.Match;
import com.arena.json.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small local server (JDK HttpServer, no framework) so the web page can start a match and watch it
 * live: it serves {@code web/}, starts matches on {@code POST /api/matches} and streams their events
 * as Server-Sent Events. The engine is unchanged: the stream is just one more event listener.
 */
public final class ArenaServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, String> TYPES = Map.of("html", "text/html; charset=utf-8",
            "js", "text/javascript; charset=utf-8", "css", "text/css; charset=utf-8", "svg", "image/svg+xml",
            "png", "image/png", "jpg", "image/jpeg", "json", "application/json", "txt", "text/plain; charset=utf-8",
            "md", "text/plain; charset=utf-8");

    private final Path webRoot;
    private final MatchFactory factory;
    private final boolean llmReady;
    private final HttpServer http;
    private final ExecutorService threads = Executors.newCachedThreadPool();
    private final Map<String, LiveMatch> matches = new ConcurrentHashMap<>();

    /** Port 0 picks a free port (tests); {@code llmReady} tells the page whether an API key is set. */
    public ArenaServer(int port, Path webRoot, MatchFactory factory, boolean llmReady) throws IOException {
        this.webRoot = webRoot.toAbsolutePath().normalize();
        this.factory = factory;
        this.llmReady = llmReady;
        this.http = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        http.setExecutor(threads);
        http.createContext("/api/options", this::options);
        http.createContext("/api/matches", this::matches);
        http.createContext("/", this::staticFile);
    }

    /** Starts listening; requests are served on background threads. */
    public void start() {
        http.start();
    }

    /** Stops listening and the running matches' streams. */
    public void stop() {
        http.stop(0);
        threads.shutdownNow();
    }

    /** The actual port, useful when 0 was asked. */
    public int port() {
        return http.getAddress().getPort();
    }

    private void options(HttpExchange exchange) throws IOException {
        send(exchange, 200, "application/json", Json.write(Map.of("bots", factory.bots(),
                "classes", factory.classes(), "llmReady", llmReady)));
    }

    private void matches(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("POST".equals(exchange.getRequestMethod()) && path.equals("/api/matches")) {
            startMatch(exchange);
            return;
        }
        String[] parts = path.split("/");
        LiveMatch match = parts.length == 5 && parts[4].equals("events") ? matches.get(parts[3]) : null;
        if (match == null) {
            send(exchange, 404, "text/plain", "Unknown match");
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, 0);
        try (Writer out = new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8)) {
            match.stream(out);
        } catch (IOException | InterruptedException e) {
            // the browser closed the page: nothing to do, the match goes on
        }
    }

    private void startMatch(HttpExchange exchange) throws IOException {
        PlayerSpec p1;
        PlayerSpec p2;
        List<String> names;
        long seed;
        try {
            JsonNode body = MAPPER.readTree(exchange.getRequestBody());
            p1 = PlayerSpec.parse(body.path("p1").asText("Aggressive:auto"));
            p2 = PlayerSpec.parse(body.path("p2").asText("Defensive:auto"));
            names = List.of(body.path("names").path(0).asText("P1").strip(), body.path("names").path(1).asText("P2").strip());
            if (names.get(0).isEmpty() || names.get(1).isEmpty() || names.get(0).equals(names.get(1))) {
                throw new IllegalArgumentException("Two different player names are needed");
            }
            seed = body.hasNonNull("seed") ? body.get("seed").asLong() : System.nanoTime() % 1_000_000;
            factory.check(p1);
            factory.check(p2);
        } catch (IllegalArgumentException | IOException e) {
            send(exchange, 400, "application/json", Json.write(Map.of("error", String.valueOf(e.getMessage()))));
            return;
        }
        String id = UUID.randomUUID().toString();
        LiveMatch live = new LiveMatch();
        matches.put(id, live);
        threads.submit(() -> play(live, p1, p2, names, seed));
        send(exchange, 201, "application/json", Json.write(Map.of("id", id)));
    }

    private void play(LiveMatch live, PlayerSpec p1, PlayerSpec p2, List<String> names, long seed) {
        try {
            Contender c1 = factory.contender(names.get(0), p1, false, seed * 2);
            Contender c2 = factory.contender(names.get(1), p2, false, seed * 2 + 1);
            EventPublisher events = new EventPublisher();
            events.subscribe(live);
            new Match(c1, c2, seed, events).play();
            live.finish(null);
        } catch (RuntimeException e) {
            live.finish(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    private void staticFile(HttpExchange exchange) throws IOException {
        String path = URI.create(exchange.getRequestURI().getRawPath()).getPath();
        Path file = webRoot.resolve(path.equals("/") ? "index.html" : path.substring(1)).normalize();
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain", "Not found");
            return;
        }
        String name = file.getFileName().toString();
        String type = TYPES.getOrDefault(name.substring(name.lastIndexOf('.') + 1), "application/octet-stream");
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void send(HttpExchange exchange, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
