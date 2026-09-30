package com.arena.log;

import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.DeckEntry;
import com.arena.engine.events.GameEvent;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MatchEnded;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.events.MinionSummoned;
import com.arena.engine.events.PlayerSetUp;
import com.arena.engine.events.TurnStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleRendererTest {

    private ByteArrayOutputStream out;
    private ConsoleRenderer renderer;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        renderer = new ConsoleRenderer(new PrintStream(out, true));
        renderer.on(new MatchStarted("Alice", "Aggressive:Mage", "Bob", "Defensive:Tank", 42L));
    }

    private List<String> lines() {
        return out.toString().lines().toList();
    }

    private void render(GameEvent... events) {
        for (GameEvent event : events) {
            renderer.on(event);
        }
    }

    @Test
    void given_a_match_start_when_rendered_then_it_prints_both_sides_and_the_seed() {
        // Given / When / Then
        assertThat(lines().getFirst())
                .isEqualTo("[MATCH  ] Alice [Aggressive:Mage] vs Bob [Defensive:Tank] | seed 42");
    }

    @Test
    void given_a_turn_start_when_rendered_then_it_prints_a_separator_and_a_padded_header() {
        // Given / When
        render(new TurnStarted(3, "Bob", 24, 1, 5, 12));

        // Then
        assertThat(lines()).contains(
                "[T03] " + "=".repeat(70),
                "[T03][Bob  ][TURN   ] [HP 24/30] [ARMOR 1] [HAND 5] [DECK 12]");
    }

    @Test
    void given_damage_with_absorbed_armor_when_rendered_then_it_shows_absorbed_and_hp_change() {
        // Given / When
        render(new TurnStarted(3, "Alice", 28, 0, 4, 13),
                new DamageDealt("Fireball", "Bob", 6, 2, 26, 22, 0));

        // Then
        assertThat(lines().getLast())
                .isEqualTo("[T03][Alice][DAMAGE ] Fireball → Bob : 6 dmg [ABSORBED 2] [HP 26 → 22]");
    }

    @Test
    void given_mana_then_a_card_played_when_rendered_then_the_card_line_shows_mana_left_over_max() {
        // Given / When
        render(new TurnStarted(3, "Alice", 28, 0, 4, 13),
                new ManaRefilled("Alice", 3, 3, false),
                new CardPlayed("Alice", "Frostbolt", 2, 1));

        // Then
        assertThat(lines().getLast()).isEqualTo("[T03][Alice][PLAY   ] Frostbolt (2) → [MANA 1/3]");
    }

    @Test
    void given_a_deck_when_set_up_is_rendered_then_cards_are_grouped_by_category_with_a_mana_curve() {
        // Given
        List<DeckEntry> deck = List.of(
                new DeckEntry("Strike", 2, "ATTACK"), new DeckEntry("Strike", 2, "ATTACK"),
                new DeckEntry("Iron Wall", 3, "DEFENSE"), new DeckEntry("Mana Crystal", 1, "RESOURCE"),
                new DeckEntry("Pyroblast", 8, "ATTACK"));

        // When
        render(new PlayerSetUp("Alice", "Aggressive", "Mage", "imposed", "preset", "Fireblast", 2,
                "deal 1 damage", deck));

        // Then
        assertThat(lines()).contains(
                "[SETUP  ][Alice] [Aggressive] plays [Mage] (imposed) | deck: preset",
                "[POWER  ][Alice] Fireblast (2) — deal 1 damage",
                "[DECK   ][Alice] [ATTACK    3] Strike x2, Pyroblast x1",
                "[DECK   ][Alice] [DEFENSE   1] Iron Wall x1",
                "[DECK   ][Alice] [RESOURCE  1] Mana Crystal x1",
                "[CURVE  ][Alice] [1: 1] [2: 2] [3: 1] [4: 0] [5+: 1] | avg cost 3.2");
    }

    @Test
    void given_a_taunt_summon_when_rendered_then_it_shows_stats_board_and_taunt() {
        // Given / When
        render(new TurnStarted(4, "Bob", 29, 0, 4, 12),
                new MinionSummoned("Bob", "Iron Golem", 2, 6, true, 1));

        // Then
        assertThat(lines().getLast()).isEqualTo("[T04][Bob  ][SUMMON ] Iron Golem [2/6] [TAUNT] → [BOARD 1/7]");
    }

    @Test
    void given_the_end_of_a_match_when_rendered_then_it_prints_the_result_summary() {
        // Given / When
        render(new MatchEnded("Alice", "HP 0", 18, "Alice", 12, 34, "Bob", 0, 21));

        // Then
        assertThat(lines().getLast()).isEqualTo(
                "[RESULT ] Alice WINS | reason: HP 0 | turns: 18 | HP Alice 12 / Bob 0 | damage Alice 34 / Bob 21");
    }
}
