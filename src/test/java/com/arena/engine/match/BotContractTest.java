package com.arena.engine.match;

import com.arena.engine.cards.Card;
import com.arena.testing.ScriptedBot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.testing.TestCards.aCard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BotContractTest {

    @Test
    void given_a_negative_hand_index_when_creating_play_card_then_it_is_rejected() {
        // Given / When / Then
        assertThatThrownBy(() -> new PlayCard(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_a_scripted_bot_when_asked_for_actions_then_it_replays_them_then_ends_turn() {
        // Given
        Bot bot = new ScriptedBot("P1", new PlayCard(0), new UseHeroPower());

        // When
        Action first = bot.nextAction(null);
        Action second = bot.nextAction(null);
        Action third = bot.nextAction(null);

        // Then
        assertThat(first).isEqualTo(new PlayCard(0));
        assertThat(second).isEqualTo(new UseHeroPower());
        assertThat(third).isEqualTo(new EndTurn());
    }

    @Test
    void given_a_bot_without_mulligan_rule_when_asked_then_it_keeps_its_whole_hand() {
        // Given
        Bot bot = view -> new EndTurn();
        List<Card> hand = List.of(aCard("Strike", 2), aCard("Pyroblast", 8));

        // When
        List<Integer> putBack = bot.mulligan(hand);

        // Then
        assertThat(putBack).isEmpty();
        assertThat(bot.name()).isNotBlank();
    }
}
