package com.arena.server;

import com.arena.cli.MatchFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ArenaServerTest {

    @TempDir
    Path web;

    private ArenaServer server;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void start() throws IOException {
        Files.writeString(web.resolve("index.html"), "<h1>Arena</h1>");
        server = new ArenaServer(0, web, new MatchFactory(), false);
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void given_the_web_folder_when_the_root_is_requested_then_index_html_is_served() throws Exception {
        // Given / When
        HttpResponse<String> response = get("/");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("<h1>Arena</h1>");
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("text/html"));
    }

    @Test
    void given_a_path_leaving_the_web_folder_when_requested_then_it_is_not_found() throws Exception {
        // Given / When
        HttpResponse<String> response = get("/../pom.xml");

        // Then
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void given_two_classic_bots_when_a_match_is_started_then_its_events_stream_from_start_to_end() throws Exception {
        // Given
        HttpResponse<String> created = post("/api/matches",
                "{\"p1\":\"Aggressive:Mage\",\"p2\":\"Defensive:Tank\",\"names\":[\"Alice\",\"Bob\"],\"seed\":42}");
        String id = created.body().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        // When
        HttpResponse<String> stream = get("/api/matches/" + id + "/events");

        // Then
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(stream.headers().firstValue("Content-Type")).hasValue("text/event-stream");
        assertThat(stream.body()).startsWith("data: {\"type\":\"MatchStarted\",\"player1\":\"Alice\"")
                .contains("\"type\":\"MatchEnded\"").endsWith("event: end\ndata: {}\n\n");
    }

    @Test
    void given_an_unknown_bot_when_a_match_is_started_then_the_error_is_returned_as_a_bad_request() throws Exception {
        // Given / When
        HttpResponse<String> response = post("/api/matches", "{\"p1\":\"Genius:Mage\",\"p2\":\"Random:auto\"}");

        // Then
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Genius");
    }

    @Test
    void given_no_api_key_when_options_are_requested_then_bots_classes_and_llm_status_are_listed() throws Exception {
        // Given / When
        HttpResponse<String> response = get("/api/options");

        // Then
        assertThat(response.body()).contains("\"bots\":[\"Aggressive\",\"Defensive\",\"Random\",\"Llm\"]")
                .contains("\"Mage\"").contains("\"llmReady\":false");
    }
}
