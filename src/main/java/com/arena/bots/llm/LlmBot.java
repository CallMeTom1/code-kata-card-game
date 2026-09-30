package com.arena.bots.llm;

import com.arena.bots.AggressiveBot;
import com.arena.engine.cards.Card;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.Speech;
import com.arena.engine.match.UseHeroPower;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * A bot whose decisions come from a language model: one call per turn returns an ordered plan, then the
 * plan is played action by action. Any illegal step or API error hands the rest of the turn to the
 * Aggressive bot, so a match never stalls on a model mistake.
 */
public final class LlmBot implements Bot {

    static final String TURN_SCHEMA = """
            {"type":"object","additionalProperties":false,"required":["thought","message","actions"],
             "properties":{"thought":{"type":"string"},"message":{"type":"string"},
              "actions":{"type":"array","items":{"type":"object","additionalProperties":false,
                "required":["type","card"],"properties":{"type":{"type":"string","enum":["PLAY","HERO_POWER"]},
                "card":{"type":"string"}}}}}}""";

    static final String MULLIGAN_SCHEMA = """
            {"type":"object","additionalProperties":false,"required":["thought","message","putBack"],
             "properties":{"thought":{"type":"string"},"message":{"type":"string"},
              "putBack":{"type":"array","items":{"type":"integer"}}}}""";

    private final LlmClient client;
    private final SpeechBuffer speech;
    private final Bot fallback = new AggressiveBot();
    private final Deque<JsonNode> plan = new ArrayDeque<>();
    private int plannedTurn = -1;
    private boolean fallingBack;

    /** A bot with its own speech buffer. */
    public LlmBot(LlmClient client) {
        this(client, new SpeechBuffer());
    }

    /** Shares the buffer with the deck strategy, so the deck choice is also said aloud. */
    LlmBot(LlmClient client, SpeechBuffer speech) {
        this.client = client;
        this.speech = speech;
    }

    @Override
    public String name() {
        return "Llm";
    }

    @Override
    public Action nextAction(GameView view) {
        if (view.turn() != plannedTurn) {
            startTurn(view);
        }
        if (fallingBack) {
            return fallback.nextAction(view);
        }
        if (plan.isEmpty()) {
            return new EndTurn();
        }
        JsonNode step = plan.poll();
        if ("HERO_POWER".equals(LlmJson.text(step, "type"))) {
            return view.heroPowerAvailable() ? new UseHeroPower() : fallBack(view, "hero power not available");
        }
        String name = LlmJson.text(step, "card");
        List<Card> hand = view.myHand();
        Optional<Integer> index = IntStream.range(0, hand.size()).boxed()
                .filter(i -> hand.get(i).name().equalsIgnoreCase(name) && hand.get(i).cost() <= view.myMana())
                .findFirst();
        return index.<Action>map(PlayCard::new)
                .orElseGet(() -> fallBack(view, "cannot play '" + name + "' (not in hand or too expensive)"));
    }

    @Override
    public List<Integer> mulligan(List<Card> openingHand) {
        try {
            JsonNode answer = LlmJson.parse(client.complete(LlmPrompts.system(), LlmPrompts.mulligan(openingHand),
                    MULLIGAN_SCHEMA));
            speech.add(LlmJson.text(answer, "thought"), LlmJson.text(answer, "message"));
            return StreamSupport.stream(answer.path("putBack").spliterator(), false).map(JsonNode::asInt)
                    .filter(i -> i >= 0 && i < openingHand.size()).distinct().toList();
        } catch (RuntimeException e) {
            speech.add("LLM error (" + e.getMessage() + "): Aggressive mulligan.", "");
            return fallback.mulligan(openingHand);
        }
    }

    @Override
    public Optional<Speech> takeSpeech() {
        return speech.take();
    }

    private void startTurn(GameView view) {
        plannedTurn = view.turn();
        plan.clear();
        fallingBack = false;
        try {
            JsonNode answer = LlmJson.parse(client.complete(LlmPrompts.system(), LlmPrompts.turn(view), TURN_SCHEMA));
            speech.add(LlmJson.text(answer, "thought"), LlmJson.text(answer, "message"));
            answer.path("actions").forEach(plan::add);
        } catch (RuntimeException e) {
            speech.add("LLM error (" + e.getMessage() + "): the Aggressive bot plays this turn.", "");
            fallingBack = true;
        }
    }

    private Action fallBack(GameView view, String reason) {
        speech.add("Plan broken: " + reason + ". The Aggressive bot finishes the turn.", "");
        fallingBack = true;
        plan.clear();
        return fallback.nextAction(view);
    }
}
