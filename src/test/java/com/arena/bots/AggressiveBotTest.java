package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.effects.BuffNextAttack;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.UseHeroPower;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.UTILITY;
import static com.arena.engine.cards.NeutralCards.CRUSHING_BLOW;
import static com.arena.engine.cards.NeutralCards.HEALING_POTION;
import static com.arena.engine.cards.NeutralCards.MANA_CRYSTAL;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.STRIKE;
import static com.arena.testing.ViewBuilder.aView;
import static org.assertj.core.api.Assertions.assertThat;

class AggressiveBotTest {

    private static final Card BATTLE_CRY = card("Battle Cry", 1, UTILITY).effect(new BuffNextAttack(3))
            .attackBuff().build();

    private final AggressiveBot bot = new AggressiveBot();

    @Test
    void given_a_resource_and_an_attack_in_hand_when_asked_then_it_plays_the_resource_first() {
        // Given / When
        var action = bot.nextAction(aView().withMana(3).withHand(STRIKE, MANA_CRYSTAL).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(1));
    }

    @Test
    void given_two_affordable_attacks_when_asked_then_it_plays_the_highest_damage() {
        // Given / When
        var action = bot.nextAction(aView().withMana(5).withHand(QUICK_JAB, CRUSHING_BLOW, STRIKE).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(1));
    }

    @Test
    void given_battle_cry_and_an_attack_still_affordable_after_when_asked_then_it_plays_the_buff_first() {
        // Given / When
        var action = bot.nextAction(aView().withMana(3).withHand(STRIKE, BATTLE_CRY).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(1));
    }

    @Test
    void given_battle_cry_and_no_attack_affordable_after_when_asked_then_it_does_not_play_it() {
        // Given / When
        var action = bot.nextAction(aView().withMana(2).withHand(STRIKE, BATTLE_CRY).build());

        // Then
        assertThat(action).isEqualTo(new PlayCard(0));
    }

    @Test
    void given_full_hp_and_only_a_heal_when_asked_then_it_keeps_the_heal() {
        // Given / When
        var action = bot.nextAction(aView().withHp(30).withMana(2).withHand(HEALING_POTION).build());

        // Then
        assertThat(action).isEqualTo(new EndTurn());
    }

    @Test
    void given_2_mana_left_and_nothing_playable_when_asked_then_it_uses_the_hero_power() {
        // Given / When
        var action = bot.nextAction(aView().withMana(2).withHand(CRUSHING_BLOW).withHeroPowerAvailable().build());

        // Then
        assertThat(action).isEqualTo(new UseHeroPower());
    }

    @Test
    void given_the_coin_and_nothing_more_to_afford_when_asked_then_it_keeps_the_coin() {
        // Given / When
        var action = bot.nextAction(aView().withMana(0).withHand(NeutralCards.THE_COIN, CRUSHING_BLOW).build());

        // Then
        assertThat(action).isEqualTo(new EndTurn());
    }

    @Test
    void given_an_opening_hand_with_a_5_cost_card_when_mulligan_then_it_is_put_back() {
        // Given / When
        List<Integer> putBack = bot.mulligan(List.of(QUICK_JAB, CRUSHING_BLOW, STRIKE));

        // Then
        assertThat(putBack).containsExactly(1);
    }
}
