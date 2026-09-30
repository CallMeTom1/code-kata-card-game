package com.arena.log;

import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.OpeningHand;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonEventExporterTest {

    @Test
    void given_an_event_when_exported_then_one_json_line_with_its_type_and_fields_is_written() {
        // Given
        StringWriter out = new StringWriter();
        JsonEventExporter exporter = new JsonEventExporter(out);

        // When
        exporter.on(new DamageDealt("Fireball", "Bob", 6, 2, 26, 22, 0));

        // Then
        assertThat(out.toString()).isEqualTo("{\"type\":\"DamageDealt\",\"source\":\"Fireball\",\"target\":\"Bob\","
                + "\"amount\":6,\"absorbed\":2,\"hpBefore\":26,\"hpAfter\":22,\"armorAfter\":0}\n");
    }

    @Test
    void given_a_list_and_quotes_when_exported_then_they_are_valid_json() {
        // Given
        StringWriter out = new StringWriter();
        JsonEventExporter exporter = new JsonEventExporter(out);

        // When
        exporter.on(new OpeningHand("Al \"Ace\"", List.of("Strike", "Power Word: Shield")));

        // Then
        assertThat(out.toString()).isEqualTo("{\"type\":\"OpeningHand\",\"player\":\"Al \\\"Ace\\\"\","
                + "\"cards\":[\"Strike\",\"Power Word: Shield\"]}\n");
    }
}
