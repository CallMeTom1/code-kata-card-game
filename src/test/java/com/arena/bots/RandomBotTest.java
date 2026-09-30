package com.arena.bots;

import com.arena.engine.match.Action;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.PlayCard;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.arena.engine.cards.NeutralCards.CRUSHING_BLOW;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.STRIKE;
import static com.arena.testing.ViewBuilder.aView;
import static org.assertj.core.api.Assertions.assertThat;

class RandomBotTest {

    @Test
    void given_no_affordable_card_when_asked_then_it_ends_the_turn() {
        // Given
        RandomBot bot = new RandomBot(new Random(1));

        // When
        Action action = bot.nextAction(aView().withMana(1).withHand(CRUSHING_BLOW).build());

        // Then
        assertThat(action).isEqualTo(new EndTurn());
    }

    @Test
    void given_many_seeds_when_asked_then_it_never_picks_an_unaffordable_card() {
        // Given / When / Then
        for (int seed = 0; seed < 100; seed++) {
            Action action = new RandomBot(new Random(seed))
                    .nextAction(aView().withMana(2).withHand(CRUSHING_BLOW, QUICK_JAB, STRIKE).build());
            assertThat(action).isIn(new PlayCard(1), new PlayCard(2));
        }
    }

    @Test
    void given_the_same_seed_when_asked_twice_then_it_makes_the_same_choice() {
        // Given
        var view = aView().withMana(3).withHand(QUICK_JAB, STRIKE, QUICK_JAB).build();

        // When
        Action first = new RandomBot(new Random(9)).nextAction(view);
        Action second = new RandomBot(new Random(9)).nextAction(view);

        // Then
        assertThat(first).isEqualTo(second);
    }
}
