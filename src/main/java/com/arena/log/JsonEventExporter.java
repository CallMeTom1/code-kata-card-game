package com.arena.log;

import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Writes each event as one JSON line, so a future HTML/JS front can replay a match
 * without any change to the engine. No library: events are simple records.
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
            out.write(toJson((Record) event, true));
            out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toJson(Record record, boolean withType) {
        StringBuilder json = new StringBuilder("{");
        String separator = "";
        if (withType) {
            json.append("\"type\":").append(quote(record.getClass().getSimpleName()));
            separator = ",";
        }
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            json.append(separator).append(quote(component.getName())).append(':')
                    .append(value(read(component, record)));
            separator = ",";
        }
        return json.append('}').toString();
    }

    private static Object read(RecordComponent component, Record record) {
        try {
            return component.getAccessor().invoke(record);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read " + component.getName(), e);
        }
    }

    private static String value(Object value) {
        return switch (value) {
            case null -> "null";
            case Number number -> number.toString();
            case Boolean bool -> bool.toString();
            case Record nested -> toJson(nested, false);
            case Collection<?> list -> list.stream().map(JsonEventExporter::value)
                    .collect(Collectors.joining(",", "[", "]"));
            default -> quote(value.toString());
        };
    }

    private static String quote(String text) {
        StringBuilder quoted = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                default -> quoted.append(c < 0x20 ? String.format("\\u%04x", (int) c) : String.valueOf(c));
            }
        }
        return quoted.append('"').toString();
    }
}
