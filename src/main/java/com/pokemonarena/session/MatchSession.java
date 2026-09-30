package com.pokemonarena.session;

import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.deck.DeckLists;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;
import com.pokemonarena.game.Match;
import com.pokemonarena.game.MatchListener;
import com.pokemonarena.game.MatchResult;
import com.pokemonarena.game.Phase;
import com.pokemonarena.game.PlayerState;

import java.util.Optional;
import java.util.Random;

/**
 * Drives one {@link Match} for both game modes with the same engine. Each side is controlled
 * either by a {@link BotStrategy} or by a human (no Bot): the only difference between the
 * modes is who produces the next {@link Action}.
 * <ul>
 *     <li>Human vs AI: call {@link #advance()}, then {@link #submit(Action)} whenever
 *     {@link #isAwaitingHuman()}; the Bot's turns are played automatically.</li>
 *     <li>AI vs AI: call {@link #playToEnd()}.</li>
 * </ul>
 */
public final class MatchSession {

    /** Safety net against a misbehaving Bot: its turn is ended after this many actions. */
    public static final int MAX_ACTIONS_PER_TURN = 100;

    private final Match match;
    private final BotStrategy bot1;
    private final BotStrategy bot2;

    /** @param bot1 Bot of side 1, or {@code null} for a human; same for {@code bot2} */
    public MatchSession(Match match, BotStrategy bot1, BotStrategy bot2) {
        this.match = match;
        this.bot1 = bot1;
        this.bot2 = bot2;
    }

    /** Creates a seeded match: the single {@link Random} shuffles both decks and flips the coin. */
    public static Match newMatch(PlayerSetup side1, PlayerSetup side2, long seed, MatchListener listener) {
        Random random = new Random(seed);
        Deck deck1 = Deck.shuffled(DeckLists.cards(side1.deck()), random);
        Deck deck2 = Deck.shuffled(DeckLists.cards(side2.deck()), random);
        return new Match(side1.hero(), deck1, side2.hero(), deck2, random, listener);
    }

    public static MatchSession humanVsAi(PlayerSetup human, PlayerSetup ai, BotStrategy aiBot,
                                         long seed, MatchListener listener) {
        return new MatchSession(newMatch(human, ai, seed, listener), null, aiBot);
    }

    public static MatchSession aiVsAi(PlayerSetup side1, BotStrategy bot1, PlayerSetup side2, BotStrategy bot2,
                                      long seed, MatchListener listener) {
        return new MatchSession(newMatch(side1, side2, seed, listener), bot1, bot2);
    }

    public Match match() {
        return match;
    }

    /** A fresh immutable snapshot of the match, seen from the side that must act now. */
    public GameView view() {
        return new GameView(match);
    }

    public boolean isOver() {
        return match.isOver();
    }

    public Optional<MatchResult> result() {
        return match.isOver() ? Optional.of(match.result()) : Optional.empty();
    }

    /** True when the PLAY phase is open for a human-controlled side. */
    public boolean isAwaitingHuman() {
        return !match.isOver() && match.phase() == Phase.PLAY && botOf(match.active()) == null;
    }

    /**
     * Moves the match forward: begins turns and plays every Bot turn, until the match is over
     * or a human must act.
     */
    public void advance() {
        while (!match.isOver()) {
            if (match.phase() == Phase.END) {
                match.beginTurn();
            }
            BotStrategy bot = botOf(match.active());
            if (bot == null) {
                return;
            }
            playBotTurn(bot);
        }
    }

    /** Submits the human's action (validated by the engine), then lets the AI play if the turn ended. */
    public void submit(Action action) {
        if (!isAwaitingHuman()) {
            throw new IllegalStateException("no human action expected now");
        }
        match.perform(action);
        advance();
    }

    /** AI vs AI: plays the whole match. */
    public MatchResult playToEnd() {
        if (bot1 == null || bot2 == null) {
            throw new IllegalStateException("playToEnd requires a Bot on both sides");
        }
        advance();
        return match.result();
    }

    private void playBotTurn(BotStrategy bot) {
        for (int i = 0; i < MAX_ACTIONS_PER_TURN && !match.isOver() && match.phase() == Phase.PLAY; i++) {
            match.perform(bot.chooseAction(view()));
        }
        if (!match.isOver() && match.phase() == Phase.PLAY) {
            match.endTurn();
        }
    }

    private BotStrategy botOf(PlayerState player) {
        return player == match.player1() ? bot1 : bot2;
    }
}
