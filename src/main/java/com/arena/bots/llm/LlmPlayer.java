package com.arena.bots.llm;

import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.match.Bot;

/** The two halves of one AI player (deck choice and play), sharing one voice for the logs. */
public final class LlmPlayer {

    private final LlmBot bot;
    private final LlmDeckStrategy deckStrategy;

    /** A fresh player per match, so no plan or speech leaks from one match to the next. */
    public LlmPlayer(LlmClient client) {
        SpeechBuffer speech = new SpeechBuffer();
        this.bot = new LlmBot(client, speech);
        this.deckStrategy = new LlmDeckStrategy(client, speech);
    }

    /** Plays the match. */
    public Bot bot() {
        return bot;
    }

    /** Picks the class and deck before the match. */
    public DeckStrategy deckStrategy() {
        return deckStrategy;
    }
}
