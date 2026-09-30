package com.arena.bots;

import com.arena.engine.match.PlayCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.engine.cards.NeutralCards.CRUSHING_BLOW;
import static com.arena.engine.cards.NeutralCards.HEALING_POTION;
import static com.arena.engine.cards.NeutralCards.IRON_WALL;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.STRIKE;
import static com.arena.testing.ViewBuilder.aView;
import static org.assertj.core.api.Assertions.assertThat;

class DefensiveBotTest {

    private final DefensiveBot bot = new DefensiveBot();

    @Test
    void given_15_hp_with_a_heal_and_an_attack_when_asked_then_it_heals_first() {
        // Given / When
        var action = bot.nextAction(aView().withHp(15).withMana(4).withHand(STRIKE, HEALING_POTION).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(1));
    }

    @Test
    void given_20_hp_with_a_heal_and_an_attack_when_asked_then_it_attacks_like_aggressive() {
        // Given / When
        var action = bot.nextAction(aView().withHp(20).withMana(4).withHand(STRIKE, HEALING_POTION).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(0));
    }

    @Test
    void given_low_hp_no_heal_and_a_defense_when_asked_then_it_plays_the_defense_before_attacking() {
        // Given / When
        var action = bot.nextAction(aView().withHp(10).withMana(5).withHand(STRIKE, IRON_WALL).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(1));
    }

    @Test
    void given_an_opening_hand_when_mulligan_then_only_cards_costing_5_or_more_go_back() {
        // Given / When
        List<Integer> putBack = bot.mulligan(List.of(QUICK_JAB, IRON_WALL, CRUSHING_BLOW));

        // Then
        assertThat(putBack).containsExactly(2);
    }
}
