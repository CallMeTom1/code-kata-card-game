package com.pokemonarena.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Serves the files of the {@code frontend/} directory (GET only, no directory listing). */
final class StaticFileHandler implements HttpHandler {

    private static final Map<String, String> TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "png", "image/png",
            "svg", "image/svg+xml",
            "ico", "image/x-icon");

    private final Path root;

    StaticFileHandler(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("HEAD")) {
            Http.sendError(exchange, 405, "method not allowed");
            return;
        }
        String path = URLDecoder.decode(exchange.getRequestURI().getPath(), StandardCharsets.UTF_8);
        if (path.endsWith("/")) {
            path += "index.html";
        }
        Path file = root.resolve(path.substring(1)).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            Http.send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String name = file.getFileName().toString();
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        Http.send(exchange, 200, TYPES.getOrDefault(extension, "application/octet-stream"), Files.readAllBytes(file));
    }
}
