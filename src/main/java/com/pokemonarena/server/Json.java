package com.pokemonarena.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON writer and parser, limited to what the API needs: objects are {@link Map}s,
 * arrays are {@link List}s, plus strings, numbers, booleans and {@code null}. Parsed numbers
 * are {@link Long} when integral, otherwise {@link Double}.
 */
public final class Json {

    private Json() {
    }

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String text) {
            writeString(text, out);
        } else if (value instanceof Double number && !Double.isFinite(number)) {
            out.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Enum<?> constant) {
            writeString(constant.name(), out);
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                writeString(String.valueOf(entry.getKey()), out);
                out.append(':');
                write(entry.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof Iterable<?> items) {
            out.append('[');
            boolean first = true;
            for (Object item : items) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                write(item, out);
            }
            out.append(']');
        } else {
            throw new IllegalArgumentException("cannot write " + value.getClass().getName() + " as JSON");
        }
    }

    private static void writeString(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    /** Parses a JSON document; throws {@link IllegalArgumentException} on malformed input. */
    public static Object parse(String text) {
        Parser parser = new Parser(text);
        Object value = parser.value();
        parser.skipWhitespace();
        if (parser.pos != text.length()) {
            throw parser.error("unexpected trailing content");
        }
        return value;
    }

    private static final class Parser {
        private final String text;
        private int pos;

        Parser(String text) {
            this.text = text;
        }

        Object value() {
            skipWhitespace();
            if (pos >= text.length()) {
                throw error("unexpected end of input");
            }
            char c = text.charAt(pos);
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> number();
            };
        }

        private Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++;
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                if (peek() != '"') {
                    throw error("expected a string key");
                }
                String key = string();
                skipWhitespace();
                expect(':');
                map.put(key, value());
                skipWhitespace();
                if (peek() == ',') {
                    pos++;
                } else {
                    expect('}');
                    return map;
                }
            }
        }

        private List<Object> array() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(value());
                skipWhitespace();
                if (peek() == ',') {
                    pos++;
                } else {
                    expect(']');
                    return list;
                }
            }
        }

        private String string() {
            pos++;
            StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') {
                    return out.toString();
                }
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (pos >= text.length()) {
                    break;
                }
                char escaped = text.charAt(pos++);
                switch (escaped) {
                    case '"', '\\', '/' -> out.append(escaped);
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'u' -> {
                        if (pos + 4 > text.length()) {
                            throw error("bad unicode escape");
                        }
                        out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw error("bad escape \\" + escaped);
                }
            }
            throw error("unterminated string");
        }

        private Object number() {
            int start = pos;
            while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
                pos++;
            }
            String token = text.substring(start, pos);
            if (token.isEmpty()) {
                throw error("unexpected character '" + text.charAt(start) + "'");
            }
            try {
                if (token.contains(".") || token.contains("e") || token.contains("E")) {
                    return Double.parseDouble(token);
                }
                return Long.parseLong(token);
            } catch (NumberFormatException e) {
                throw error("bad number " + token);
            }
        }

        private Object literal(String word, Object value) {
            if (!text.startsWith(word, pos)) {
                throw error("unexpected token");
            }
            pos += word.length();
            return value;
        }

        private char peek() {
            return pos < text.length() ? text.charAt(pos) : '\0';
        }

        private void expect(char c) {
            if (peek() != c) {
                throw error("expected '" + c + "'");
            }
            pos++;
        }

        void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException("invalid JSON at " + pos + ": " + message);
        }
    }
}
