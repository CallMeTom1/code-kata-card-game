package com.arena.json;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Minimal JSON writer for records, maps, lists and scalars, so exports need no library. */
public final class Json {

    private Json() {
    }

    /** Serializes a value; records keep their component order so the output is stable. */
    public static String write(Object value) {
        return switch (value) {
            case null -> "null";
            case Number number -> number.toString();
            case Boolean bool -> bool.toString();
            case Record record -> writeRecord(record);
            case Map<?, ?> map -> map.entrySet().stream()
                    .map(e -> quote(String.valueOf(e.getKey())) + ":" + write(e.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
            case Collection<?> list -> list.stream().map(Json::write).collect(Collectors.joining(",", "[", "]"));
            default -> quote(value.toString());
        };
    }

    /** A record with an extra leading "type" field, used for event streams. */
    public static String writeTyped(Record record) {
        String body = writeRecord(record);
        String type = "\"type\":" + quote(record.getClass().getSimpleName());
        return body.equals("{}") ? "{" + type + "}" : "{" + type + "," + body.substring(1);
    }

    private static String writeRecord(Record record) {
        StringBuilder json = new StringBuilder("{");
        String separator = "";
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            json.append(separator).append(quote(component.getName())).append(':').append(write(read(component, record)));
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
