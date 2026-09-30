package com.arena.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Settings read from the environment, then {@code .env.local} (private, git-ignored), then {@code .env}
 * (committed placeholders), so a secret never has to be committed.
 */
public final class EnvFile {

    private final Map<String, String> values;

    private EnvFile(Map<String, String> values) {
        this.values = values;
    }

    /** Merges the two files of {@code dir} under the real environment, which always wins. */
    public static EnvFile load(Path dir, Map<String, String> environment) {
        Map<String, String> values = new HashMap<>(read(dir.resolve(".env")));
        values.putAll(read(dir.resolve(".env.local")));
        environment.forEach((key, value) -> {
            if (!value.isBlank()) {
                values.put(key, value);
            }
        });
        return new EnvFile(values);
    }

    /** The value of {@code key}, absent when no source defines it. */
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key)).filter(value -> !value.isBlank());
    }

    private static Map<String, String> read(Path file) {
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(file)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.strip();
                int equals = trimmed.indexOf('=');
                if (trimmed.isEmpty() || trimmed.startsWith("#") || equals < 1) {
                    continue;
                }
                values.put(trimmed.substring(0, equals).strip(), unquote(trimmed.substring(equals + 1).strip()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return values;
    }

    private static String unquote(String value) {
        boolean quoted = value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                || value.startsWith("'") && value.endsWith("'"));
        return quoted ? value.substring(1, value.length() - 1) : value;
    }
}
