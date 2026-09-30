package com.pokemonarena.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Small helpers shared by the handlers: read a JSON body, write a JSON response. */
final class Http {

    private Http() {
    }

    static Map<String, Object> readJson(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return JsonMapper.object(Json.parse(body.isBlank() ? "{}" : body));
    }

    static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", Json.write(body).getBytes(StandardCharsets.UTF_8));
    }

    static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message == null ? "error" : message));
    }

    static void send(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
