package com.arena.engine.match;

import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.effects.Effect;
import com.arena.engine.effects.Summon;
import com.arena.engine.events.EventPublisher;
import com.arena.testing.ScriptedBot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.arena.testing.TestMatches.contender;
import static com.arena.testing.TestMatches.deckOf;
import static com.arena.testing.TestMatches.p1First;
import static com.arena.testing.TestMatches.testClass;
import static org.assertj.core.api.Assertions.assertThat;

class GameViewContentTest {

    private static final Card WOLF = new Card("Wild Wolf", 1, CardCategory.ATTACK,
            new Summon(new MinionTemplate("Wolf", 2, 2, false), 1), false);

    /** Keeps every view it receives, so a test can check what a bot was shown. */
    private static final class WatchingBot extends ScriptedBot {
        private final List<GameView> seen = new ArrayList<>();

        WatchingBot(String name, Action... actions) {
            super(name, actions);
        }

        @Override
        public Action nextAction(GameView view) {
            seen.add(view);
            return super.nextAction(view);
        }
    }

    @Test
    void given_an_enemy_wolf_on_the_board_when_the_other_bot_plays_then_its_view_shows_minions_and_classes() {
        // Given
        WatchingBot p2 = new WatchingBot("P2");
        Contender c1 = contender("P1", new ScriptedBot("P1", new PlayCard(0)), testClass(Effect.NONE),
                deckOf(WOLF, 20));
        Contender c2 = new Contender("P2", p2, new com.arena.engine.classes.HeroClass("Mage",
                testClass(Effect.NONE).heroPower(), List.of(), List.of()), deckOf(WOLF, 20), "imposed", "preset");

        // When
        p1First(c1, c2, new EventPublisher()).play();

        // Then
        GameView view = p2.seen.getFirst();
        assertThat(view.myClass()).isEqualTo("Mage");
        assertThat(view.opponentClass()).isEqualTo("Tester");
        assertThat(view.opponentMinions()).containsExactly(new MinionView("Wolf", 2, 2, false, false));
        assertThat(view.myMinions()).isEmpty();
        assertThat(view.myMaxMana()).isEqualTo(1);
        assertThat(view.myDeckSize()).isEqualTo(15);
    }
}
