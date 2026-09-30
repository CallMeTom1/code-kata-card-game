package com.pokemonarena.server;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTest {

    @Test
    void writesValues() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("s", "a\"b\\c\né");
        map.put("n", 3);
        map.put("d", 0.5);
        map.put("b", true);
        map.put("z", null);
        map.put("l", List.of(1, "x"));
        map.put("e", Thread.State.NEW);

        assertEquals("{\"s\":\"a\\\"b\\\\c\\né\",\"n\":3,\"d\":0.5,\"b\":true,\"z\":null,\"l\":[1,\"x\"],\"e\":\"NEW\"}",
                Json.write(map));
    }

    @Test
    void parsesWhatItWrites() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("s", "a\"b\\c\n\t/é");
        map.put("n", -42L);
        map.put("d", 1.25);
        map.put("b", false);
        map.put("z", null);
        map.put("l", Arrays.asList(1L, "x", List.of(), Map.of()));

        assertEquals(map, Json.parse(Json.write(map)));
    }

    @Test
    void parsesWhitespaceAndUnicodeEscapes() {
        assertEquals(Map.of("k", List.of("é", 7L)), Json.parse(" { \"k\" : [ \"\\u00e9\" , 7 ] } "));
    }

    @Test
    void rejectsMalformedJson() {
        for (String bad : List.of("", "{", "{\"a\":}", "[1,]", "{\"a\":1} x", "tru", "\"open", "{a:1}")) {
            assertThrows(IllegalArgumentException.class, () -> Json.parse(bad), bad);
        }
    }
}
