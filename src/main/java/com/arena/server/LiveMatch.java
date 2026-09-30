package com.arena.server;

import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;
import com.arena.json.Json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A match being played on the server: it keeps every event as a JSON line, so a browser that
 * connects late still receives the whole match, then waits for the next ones.
 */
final class LiveMatch implements GameEventListener {

    private final List<String> lines = new ArrayList<>();
    private boolean finished;
    private String failure;

    @Override
    public synchronized void on(GameEvent event) {
        lines.add(Json.writeTyped((Record) event));
        notifyAll();
    }

    /** Ends the stream; {@code failure} is null when the match ended normally. */
    synchronized void finish(String failure) {
        this.failure = failure;
        this.finished = true;
        notifyAll();
    }

    /** Writes every event as Server-Sent Events, blocking until the match ends. */
    void stream(Writer out) throws IOException, InterruptedException {
        int sent = 0;
        while (true) {
            List<String> batch;
            boolean done;
            String error;
            synchronized (this) {
                while (sent == lines.size() && !finished) {
                    wait();
                }
                batch = List.copyOf(lines.subList(sent, lines.size()));
                done = finished;
                error = failure;
            }
            for (String line : batch) {
                out.write("data: " + line + "\n\n");
            }
            sent += batch.size();
            if (done) {
                out.write(error == null ? "event: end\ndata: {}\n\n"
                        : "event: failure\ndata: " + Json.write(Map.of("message", error)) + "\n\n");
                out.flush();
                return;
            }
            out.flush();
        }
    }
}
