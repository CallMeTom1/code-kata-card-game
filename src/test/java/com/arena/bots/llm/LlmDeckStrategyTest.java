package com.arena.bots.llm;

import com.arena.bots.AggressiveDeckStrategy;
import com.arena.engine.cards.Card;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.classes.StandardClasses;
import com.arena.testing.FakeLlmClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LlmDeckStrategyTest {

    private static final HeroClasses CLASSES = StandardClasses.all();
    private static final HeroClass MAGE = CLASSES.byName("Mage").orElseThrow();
    private static final HeroClass TANK = CLASSES.byName("Tank").orElseThrow();

    private static String answer(String heroClass, List<Card> deck) {
        String names = deck.stream().map(c -> "\"" + c.name() + "\"").collect(Collectors.joining(","));
        return "{\"thought\":\"Burn wins races.\",\"message\":\"Prepare to burn!\",\"heroClass\":\"" + heroClass
                + "\",\"deck\":[" + names + "]}";
    }

    @Test
    void given_an_auto_class_when_the_llm_picks_mage_and_a_legal_deck_then_both_are_used_with_one_call() {
        // Given
        FakeLlmClient client = new FakeLlmClient().answering(answer("Mage", MAGE.presetDeck()));
        LlmDeckStrategy strategy = new LlmDeckStrategy(client, new SpeechBuffer());

        // When
        HeroClass chosen = strategy.chooseClass(CLASSES);
        List<Card> deck = strategy.buildDeck(chosen, NeutralCards.all());

        // Then
        assertThat(chosen).isEqualTo(MAGE);
        assertThat(deck).containsExactlyElementsOf(MAGE.presetDeck());
        assertThat(client.prompts()).hasSize(1);
    }

    @Test
    void given_an_imposed_class_when_building_the_deck_then_the_prompt_names_that_class() {
        // Given
        FakeLlmClient client = new FakeLlmClient().answering(answer("Tank", TANK.presetDeck()));
        LlmDeckStrategy strategy = new LlmDeckStrategy(client, new SpeechBuffer());

        // When
        List<Card> deck = strategy.buildDeck(TANK, NeutralCards.all());

        // Then
        assertThat(deck).containsExactlyElementsOf(TANK.presetDeck());
        assertThat(client.prompts().getFirst()).contains("Your class is imposed: Tank");
    }

    @Test
    void given_an_illegal_deck_when_building_then_the_llm_gets_the_errors_and_one_more_try() {
        // Given
        FakeLlmClient client = new FakeLlmClient().answering(answer("Tank", TANK.presetDeck().subList(0, 19)),
                answer("Tank", TANK.presetDeck()));
        LlmDeckStrategy strategy = new LlmDeckStrategy(client, new SpeechBuffer());

        // When
        List<Card> deck = strategy.buildDeck(TANK, NeutralCards.all());

        // Then
        assertThat(deck).containsExactlyElementsOf(TANK.presetDeck());
        assertThat(client.prompts().get(1)).contains("20");
    }

    @Test
    void given_two_illegal_decks_when_building_then_the_aggressive_deck_is_used() {
        // Given
        String bad = answer("Tank", List.of(NeutralCards.STRIKE));
        LlmDeckStrategy strategy = new LlmDeckStrategy(new FakeLlmClient().answering(bad, bad), new SpeechBuffer());

        // When
        List<Card> deck = strategy.buildDeck(TANK, NeutralCards.all());

        // Then
        assertThat(deck).isEqualTo(new AggressiveDeckStrategy().buildDeck(TANK, NeutralCards.all()));
    }

    @Test
    void given_an_api_error_when_choosing_the_class_then_the_aggressive_default_class_is_used() {
        // Given
        SpeechBuffer speech = new SpeechBuffer();
        LlmDeckStrategy strategy = new LlmDeckStrategy(
                new FakeLlmClient().failingWith(new IllegalStateException("no credit")), speech);

        // When
        HeroClass chosen = strategy.chooseClass(CLASSES);

        // Then
        assertThat(chosen.name()).isEqualTo(AggressiveDeckStrategy.DEFAULT_CLASS);
        assertThat(speech.take().orElseThrow().thought()).contains("no credit");
    }
}
