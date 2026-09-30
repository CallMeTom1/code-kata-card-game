package com.arena.cli;

import com.arena.engine.match.Contender;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchFactoryTest {

    private final MatchFactory factory = new MatchFactory();

    @Test
    void given_aggressive_auto_when_building_the_contender_then_it_picks_assassin_with_a_built_deck() {
        // Given / When
        Contender contender = factory.contender("P1", new PlayerSpec("Aggressive", "auto"), false, 42L);

        // Then
        assertThat(contender.heroClass().name()).isEqualTo("Assassin");
        assertThat(contender.classChoice()).isEqualTo("auto");
        assertThat(contender.deckSource()).isEqualTo("built");
        assertThat(contender.deck()).hasSize(20);
    }

    @Test
    void given_an_imposed_class_and_preset_decks_when_building_then_the_preset_deck_is_used() {
        // Given / When
        Contender contender = factory.contender("P1", new PlayerSpec("defensive", "mage"), true, 42L);

        // Then
        assertThat(contender.heroClass().name()).isEqualTo("Mage");
        assertThat(contender.deck()).isEqualTo(contender.heroClass().presetDeck());
        assertThat(contender.bot().name()).isEqualTo("Defensive");
    }

    @Test
    void given_an_unknown_bot_when_building_then_the_error_lists_the_known_bots() {
        // Given / When / Then
        assertThatThrownBy(() -> factory.contender("P1", new PlayerSpec("Genius", "Mage"), false, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genius").hasMessageContaining("Aggressive, Defensive, Random");
    }

    @Test
    void given_an_unknown_class_when_building_then_the_error_lists_the_known_classes() {
        // Given / When / Then
        assertThatThrownBy(() -> factory.contender("P1", new PlayerSpec("Random", "Necromancer"), false, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Necromancer").hasMessageContaining("Mage, Tank, Swordsman, Assassin, Cleric");
    }
}
