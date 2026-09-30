package com.arena.bots.llm;

import com.arena.bots.AggressiveBot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.Speech;
import com.arena.engine.match.UseHeroPower;
import com.arena.testing.FakeLlmClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.STRIKE;
import static com.arena.testing.ViewBuilder.aView;
import static org.assertj.core.api.Assertions.assertThat;

class LlmBotTest {

    private static final String PLAY_STRIKE = """
            {"thought":"Strike hits hardest.","message":"Here comes the pain!",
             "actions":[{"type":"PLAY","card":"Strike"}]}""";

    @Test
    void given_a_plan_playing_strike_when_the_turn_is_played_then_strike_is_played_then_the_turn_ends() {
        // Given
        FakeLlmClient client = new FakeLlmClient().answering(PLAY_STRIKE);
        LlmBot bot = new LlmBot(client);
        GameView view = aView().withMana(3).withHand(QUICK_JAB, STRIKE).build();

        // When
        var first = bot.nextAction(view);
        var second = bot.nextAction(aView().withMana(1).withHand(QUICK_JAB).build());

        // Then
        assertThat(first).isEqualTo(new PlayCard(1));
        assertThat(second).isEqualTo(new EndTurn());
        assertThat(client.prompts()).hasSize(1);
    }

    @Test
    void given_a_plan_with_the_hero_power_when_it_is_available_then_the_bot_uses_it() {
        // Given
        LlmBot bot = new LlmBot(new FakeLlmClient().answering("""
                {"thought":"","message":"","actions":[{"type":"HERO_POWER","card":""}]}"""));

        // When
        var action = bot.nextAction(aView().withMana(2).withHeroPowerAvailable().build());

        // Then
        assertThat(action).isEqualTo(new UseHeroPower());
    }

    @Test
    void given_a_plan_naming_a_card_not_in_hand_when_played_then_the_bot_falls_back_to_the_aggressive_bot() {
        // Given
        LlmBot bot = new LlmBot(new FakeLlmClient().answering("""
                {"thought":"","message":"","actions":[{"type":"PLAY","card":"Pyroblast"}]}"""));
        GameView view = aView().withMana(3).withHand(QUICK_JAB, STRIKE).build();

        // When
        var action = bot.nextAction(view);

        // Then
        assertThat(action).isEqualTo(new AggressiveBot().nextAction(view));
        assertThat(bot.takeSpeech().orElseThrow().thought()).contains("Pyroblast");
    }

    @Test
    void given_an_api_error_when_asked_for_an_action_then_the_bot_falls_back_to_the_aggressive_bot() {
        // Given
        LlmBot bot = new LlmBot(new FakeLlmClient().failingWith(new IllegalStateException("overloaded")));
        GameView view = aView().withMana(3).withHand(QUICK_JAB, STRIKE).build();

        // When
        var action = bot.nextAction(view);

        // Then
        assertThat(action).isEqualTo(new AggressiveBot().nextAction(view));
        assertThat(bot.takeSpeech().orElseThrow().thought()).contains("overloaded");
    }

    @Test
    void given_a_plan_with_words_when_the_turn_starts_then_the_bot_hands_them_to_the_engine_once() {
        // Given
        LlmBot bot = new LlmBot(new FakeLlmClient().answering(PLAY_STRIKE));
        bot.nextAction(aView().withMana(3).withHand(STRIKE).build());

        // When
        var speech = bot.takeSpeech();

        // Then
        assertThat(speech).contains(new Speech("Strike hits hardest.", "Here comes the pain!"));
        assertThat(bot.takeSpeech()).isEmpty();
    }

    @Test
    void given_the_opponent_spoke_when_the_bot_plans_then_the_prompt_quotes_the_opponent_and_the_hand() {
        // Given
        FakeLlmClient client = new FakeLlmClient().answering(PLAY_STRIKE);
        LlmBot bot = new LlmBot(client);

        // When
        bot.nextAction(aView().withMana(3).withHand(STRIKE).withOpponentWords("You are doomed.").build());

        // Then
        assertThat(client.prompts().getFirst()).contains("You are doomed.").contains("Strike");
    }

    @Test
    void given_an_opening_hand_when_mulligan_then_the_valid_positions_chosen_by_the_llm_go_back() {
        // Given
        LlmBot bot = new LlmBot(new FakeLlmClient().answering("""
                {"thought":"Too slow.","message":"Let's shuffle.","putBack":[1,7]}"""));

        // When
        List<Integer> putBack = bot.mulligan(List.of(QUICK_JAB, STRIKE, QUICK_JAB));

        // Then
        assertThat(putBack).containsExactly(1);
    }
}
