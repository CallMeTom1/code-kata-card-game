package com.arena.log;

import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;
import com.arena.json.Json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Writes each event as one JSON line, so the HTML/JS replay in web/ can show a match
 * without any change to the engine.
 */
public final class JsonEventExporter implements GameEventListener {

    private final Writer out;

    /** Writes to any writer (a file in Main, a string in tests). */
    public JsonEventExporter(Writer out) {
        this.out = out;
    }

    @Override
    public void on(GameEvent event) {
        try {
            out.write(Json.writeTyped((Record) event));
            out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
