package com.arena.engine.match;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.events.BotSpoke;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.GameEvent;
import com.arena.testing.RecordingListener;
import com.arena.testing.ScriptedBot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.arena.testing.TestMatches.contender;
import static com.arena.testing.TestMatches.deckOf;
import static com.arena.testing.TestMatches.p1First;
import static org.assertj.core.api.Assertions.assertThat;

class BotSpeechTest {

    private static final Card JAB = new Card("Jab", 1, CardCategory.ATTACK, new DealDamage(1), false);

    /** Says one line with its first action, then stays silent. */
    private static final class TalkingBot extends ScriptedBot {
        private Speech pending;

        TalkingBot(String name, Speech speech, Action... actions) {
            super(name, actions);
            this.pending = speech;
        }

        @Override
        public Optional<Speech> takeSpeech() {
            Optional<Speech> speech = Optional.ofNullable(pending);
            pending = null;
            return speech;
        }
    }

    /** Remembers what the opponent said in each view. */
    private static final class ListeningBot extends ScriptedBot {
        private final List<String> heard = new ArrayList<>();

        ListeningBot(String name) {
            super(name);
        }

        @Override
        public Action nextAction(GameView view) {
            heard.add(view.opponentLastWords());
            return super.nextAction(view);
        }
    }

    @Test
    void given_a_bot_that_speaks_with_its_action_when_it_plays_then_its_words_are_published_before_the_card() {
        // Given
        EventPublisher events = new EventPublisher();
        RecordingListener listener = new RecordingListener();
        events.subscribe(listener);
        TalkingBot p1 = new TalkingBot("P1", new Speech("Jab is cheap.", "Take that!"), new PlayCard(0));

        // When
        p1First(contender("P1", p1, deckOf(JAB, 20)), contender("P2", new ScriptedBot("P2"), deckOf(JAB, 20)),
                events).play();

        // Then
        List<GameEvent> all = listener.events();
        BotSpoke spoke = listener.eventsOfType(BotSpoke.class).getFirst();
        assertThat(spoke).isEqualTo(new BotSpoke("P1", "Jab is cheap.", "Take that!"));
        assertThat(all.indexOf(spoke)).isLessThan(all.indexOf(listener.eventsOfType(CardPlayed.class).getFirst()));
    }

    @Test
    void given_a_bot_that_spoke_when_the_opponent_plays_then_its_view_carries_those_words() {
        // Given
        ListeningBot p2 = new ListeningBot("P2");
        TalkingBot p1 = new TalkingBot("P1", new Speech("", "Your move."));

        // When
        p1First(contender("P1", p1, deckOf(JAB, 20)), contender("P2", p2, deckOf(JAB, 20)), new EventPublisher())
                .play();

        // Then
        assertThat(p2.heard.getFirst()).isEqualTo("Your move.");
    }
}
