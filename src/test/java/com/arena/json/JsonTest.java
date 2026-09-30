package com.arena.json;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

    record Point(int x, String label) {
    }

    @Test
    void given_a_record_when_written_then_its_components_become_fields_in_order() {
        // Given / When / Then
        assertThat(Json.write(new Point(3, "a"))).isEqualTo("{\"x\":3,\"label\":\"a\"}");
    }

    @Test
    void given_maps_lists_null_and_decimals_when_written_then_they_are_valid_json() {
        // Given
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rate", 46.7);
        map.put("winner", null);
        map.put("reasons", List.of("HP 0", "turn \"limit\""));

        // When / Then
        assertThat(Json.write(map)).isEqualTo("{\"rate\":46.7,\"winner\":null,\"reasons\":[\"HP 0\",\"turn \\\"limit\\\"\"]}");
    }
}
