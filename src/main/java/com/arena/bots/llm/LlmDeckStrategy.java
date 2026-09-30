package com.arena.bots.llm;

import com.arena.bots.AggressiveDeckStrategy;
import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.DeckValidator;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Lets the model pick its class and its 20 cards. The engine's {@link DeckValidator} stays the judge:
 * an illegal deck gets one more try with the errors, then the Aggressive deck is used.
 */
public final class LlmDeckStrategy implements DeckStrategy {

    static final String DECK_SCHEMA = """
            {"type":"object","additionalProperties":false,"required":["thought","message","heroClass","deck"],
             "properties":{"thought":{"type":"string"},"message":{"type":"string"},"heroClass":{"type":"string"},
              "deck":{"type":"array","items":{"type":"string"}}}}""";

    private final LlmClient client;
    private final SpeechBuffer speech;
    private final AggressiveDeckStrategy fallback = new AggressiveDeckStrategy();
    private JsonNode firstAnswer;

    /** Shares {@code speech} with the {@link LlmBot} of the same player. */
    LlmDeckStrategy(LlmClient client, SpeechBuffer speech) {
        this.client = client;
        this.speech = speech;
    }

    @Override
    public HeroClass chooseClass(HeroClasses classes) {
        try {
            firstAnswer = ask("Choose your class and build your deck.");
            Optional<HeroClass> chosen = classes.byName(LlmJson.text(firstAnswer, "heroClass"));
            if (chosen.isPresent()) {
                return chosen.get();
            }
            speech.add("Unknown class '" + LlmJson.text(firstAnswer, "heroClass") + "': default class.", "");
        } catch (RuntimeException e) {
            speech.add("LLM error (" + e.getMessage() + "): default class.", "");
        }
        firstAnswer = null;
        return fallback.chooseClass(classes);
    }

    @Override
    public List<Card> buildDeck(HeroClass heroClass, List<Card> neutralCards) {
        DeckValidator validator = new DeckValidator(neutralCards);
        List<Card> pool = Stream.concat(heroClass.classCards().stream(), neutralCards.stream()).toList();
        try {
            JsonNode answer = firstAnswer != null ? firstAnswer
                    : ask("Your class is imposed: " + heroClass.name() + ". Build your deck.");
            List<String> errors = check(answer, heroClass, pool, validator);
            if (!errors.isEmpty()) {
                answer = ask("Your class is " + heroClass.name() + ". Your deck was rejected: "
                        + String.join("; ", errors) + ". Build a legal deck of exactly "
                        + DeckValidator.DECK_SIZE + " cards.");
                errors = check(answer, heroClass, pool, validator);
            }
            if (errors.isEmpty()) {
                return cards(answer, pool);
            }
            speech.add("Deck still illegal (" + String.join("; ", errors) + "): Aggressive deck.", "");
        } catch (RuntimeException e) {
            speech.add("LLM error (" + e.getMessage() + "): Aggressive deck.", "");
        }
        return fallback.buildDeck(heroClass, neutralCards);
    }

    private JsonNode ask(String instruction) {
        JsonNode answer = LlmJson.parse(client.complete(LlmPrompts.system(), instruction + """
                 Rules: exactly 20 cards, at most 2 copies of a card, at least 6 cards of your class, only your
                 class cards and neutral cards. Use the exact card names. Put the class name in "heroClass".""",
                DECK_SCHEMA));
        speech.add(LlmJson.text(answer, "thought"), LlmJson.text(answer, "message"));
        return answer;
    }

    private static List<String> check(JsonNode answer, HeroClass heroClass, List<Card> pool, DeckValidator validator) {
        List<String> unknown = new ArrayList<>();
        answer.path("deck").forEach(name -> {
            if (find(name.asText(), pool).isEmpty()) {
                unknown.add("unknown card '" + name.asText() + "'");
            }
        });
        return unknown.isEmpty() ? validator.validate(heroClass, cards(answer, pool)) : unknown;
    }

    private static List<Card> cards(JsonNode answer, List<Card> pool) {
        List<Card> deck = new ArrayList<>();
        answer.path("deck").forEach(name -> find(name.asText(), pool).ifPresent(deck::add));
        return deck;
    }

    private static Optional<Card> find(String name, List<Card> pool) {
        return pool.stream().filter(card -> card.name().equalsIgnoreCase(name.strip())).findFirst();
    }
}
