package com.pokemonarena.server;

import com.pokemonarena.game.IllegalActionException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;

/**
 * Tiny JDK HTTP server: serves {@code frontend/} and the JSON API on the same port. It only
 * adapts HTTP to the engine; all gameplay rules stay in {@code Match}.
 * <p>
 * Usage: {@code GameServer [port] [frontendDir]}, defaults {@code 8080} and {@code frontend}.
 */
public final class GameServer {

    public static final int DEFAULT_PORT = 8080;

    private GameServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Path frontend = Path.of(args.length > 1 ? args[1] : "frontend");
        HttpServer server = start(port, frontend);
        System.out.println("Pokémon Skirmish Arena running on http://127.0.0.1:"
                + server.getAddress().getPort() + "/ (frontend: " + frontend.toAbsolutePath() + ")");
    }

    /** Starts the server; port {@code 0} picks a free port (used by the tests). */
    public static HttpServer start(int port, Path frontendDir) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        MatchRegistry registry = new MatchRegistry();
        server.createContext("/api/catalog", api(exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                Http.sendError(exchange, 405, "use GET");
                return;
            }
            Http.sendJson(exchange, 200, JsonMapper.catalog());
        }));
        server.createContext("/api/matches", api(new MatchHandler(registry)));
        server.createContext("/api/simulations", api(new SimulationHandler()));
        server.createContext("/api/", api(exchange -> Http.sendError(exchange, 404, "no such endpoint")));
        server.createContext("/", new StaticFileHandler(frontendDir));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        return server;
    }

    /** Maps engine/request exceptions to HTTP errors so handlers stay free of error plumbing. */
    private static HttpHandler api(HttpHandler handler) {
        return exchange -> {
            try {
                handler.handle(exchange);
            } catch (IllegalActionException e) {
                Http.sendError(exchange, 400, e.getMessage());
            } catch (MatchHandler.UnknownMatchException e) {
                Http.sendError(exchange, 404, e.getMessage());
            } catch (IllegalStateException e) {
                Http.sendError(exchange, 409, e.getMessage());
            } catch (IllegalArgumentException | NoSuchElementException | ArithmeticException e) {
                Http.sendError(exchange, 400, e.getMessage());
            } catch (RuntimeException e) {
                Http.sendError(exchange, 500, e.toString());
            } finally {
                exchange.close();
            }
        };
    }
}
