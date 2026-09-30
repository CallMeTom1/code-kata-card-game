package com.arena.engine.effects;

import com.arena.engine.board.MinionAbility;
import com.arena.engine.board.MinionTemplate;
import com.arena.engine.events.ArmorGained;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.Healed;
import com.arena.engine.events.MinionSummoned;
import com.arena.engine.events.SummonFizzled;
import com.arena.engine.player.Champion;
import com.arena.testing.RecordingListener;
import com.arena.testing.TestEffectContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.arena.testing.ChampionBuilder.aChampion;
import static com.arena.testing.TestCards.aCard;
import static org.assertj.core.api.Assertions.assertThat;

class EffectsTest {

    private EventPublisher events;
    private RecordingListener listener;
    private Champion caster;
    private Champion opponent;

    @BeforeEach
    void setUp() {
        events = new EventPublisher();
        listener = new RecordingListener();
        events.subscribe(listener);
        caster = aChampion().named("P1").build();
        opponent = aChampion().named("P2").build();
    }

    private TestEffectContext context() {
        return TestEffectContext.between(caster, opponent, events);
    }

    @Test
    void given_opponent_at_30_when_deal_damage_4_then_opponent_has_26() {
        // Given / When
        new DealDamage(4).apply(context());

        // Then
        assertThat(opponent.hp()).isEqualTo(26);
    }

    @Test
    void given_an_attack_bonus_of_2_when_dealing_damage_then_the_bonus_is_added() {
        // Given / When
        new DealDamage(4).apply(context().withAttackBonus(2));

        // Then
        assertThat(opponent.hp()).isEqualTo(24);
    }

    @Test
    void given_an_attack_bonus_when_multi_hit_then_each_hit_gets_the_bonus() {
        // Given / When
        new MultiHit(2, 3).apply(context().withAttackBonus(1));

        // Then
        assertThat(opponent.hp()).isEqualTo(22);
    }

    @Test
    void given_parry_1_when_multi_hit_2x3_then_each_hit_is_reduced() {
        // Given
        opponent.defenses().addParry(1, 1);

        // When
        new MultiHit(2, 3).apply(context());

        // Then
        assertThat(opponent.hp()).isEqualTo(26);
    }

    @Test
    void given_no_combo_when_eviscerate_then_3_damage_ignoring_armor() {
        // Given
        opponent.defenses().addArmor(5, 2);

        // When
        new DealDamageIgnoringArmor(3, 5).apply(context());

        // Then
        assertThat(opponent.hp()).isEqualTo(27);
    }

    @Test
    void given_combo_active_when_eviscerate_then_5_damage() {
        // Given / When
        new DealDamageIgnoringArmor(3, 5).apply(context().withCombo());

        // Then
        assertThat(opponent.hp()).isEqualTo(25);
    }

    @Test
    void given_caster_with_6_armor_when_shield_slam_then_6_damage() {
        // Given
        caster.defenses().addArmor(6, 2);

        // When
        new DamageEqualToArmor().apply(context());

        // Then
        assertThat(opponent.hp()).isEqualTo(24);
    }

    @Test
    void given_enemy_minions_when_area_damage_3_then_champion_and_each_minion_are_hit() {
        // Given
        opponent.board().summon(new MinionTemplate("Skeleton", 1, 1, false), 1);
        opponent.board().summon(new MinionTemplate("Golem", 2, 6, true), 1);

        // When
        new DamageChampionAndAllEnemyMinions(3).apply(context());

        // Then
        assertThat(opponent.hp()).isEqualTo(27);
        assertThat(opponent.board().minions()).singleElement()
                .satisfies(golem -> assertThat(golem.health()).isEqualTo(3));
    }

    @Test
    void given_20_hp_when_heal_5_then_25_and_heal_is_published() {
        // Given
        caster.loseHp(10);

        // When
        new Heal(5).apply(context().from("Healing Potion"));

        // Then
        assertThat(caster.hp()).isEqualTo(25);
        assertThat(listener.events()).containsExactly(new Healed("P1", "Healing Potion", 5, 20, 25));
    }

    @Test
    void given_no_armor_when_gain_armor_7_for_2_turns_then_armor_is_7() {
        // Given / When
        new GainArmor(7, 2).apply(context().from("Iron Wall"));

        // Then
        assertThat(caster.defenses().armor()).isEqualTo(7);
        assertThat(listener.events()).containsExactly(new ArmorGained("P1", "Iron Wall", 7, 2, 7));
    }

    @Test
    void given_a_deck_when_draw_2_then_hand_has_2_more_cards() {
        // Given
        caster = aChampion().named("P1").withDeck(aCard("A", 1), aCard("B", 1), aCard("C", 1)).build();

        // When
        new DrawCards(2).apply(context());

        // Then
        assertThat(caster.hand()).hasSize(2);
    }

    @Test
    void given_4_max_mana_when_gain_max_mana_1_then_5_max_mana() {
        // Given
        caster.refillMana(4);

        // When
        new GainMaxMana(1).apply(context());

        // Then
        assertThat(caster.maxMana()).isEqualTo(5);
    }

    @Test
    void given_1_mana_when_gain_temp_mana_2_then_3_mana() {
        // Given
        caster.refillMana(1);

        // When
        new GainTempMana(2).apply(context());

        // Then
        assertThat(caster.mana()).isEqualTo(3);
    }

    @Test
    void given_an_opponent_when_freeze_then_it_is_frozen() {
        // Given / When
        new Freeze().apply(context());

        // Then
        assertThat(opponent.isFrozen()).isTrue();
    }

    @Test
    void given_no_buff_when_buff_next_attack_3_then_caster_bonus_is_3() {
        // Given / When
        new BuffNextAttack(3).apply(context());

        // Then
        assertThat(caster.attackBonus()).isEqualTo(3);
    }

    @Test
    void given_no_poison_when_apply_poison_2_for_3_turns_then_opponent_is_poisoned() {
        // Given / When
        new ApplyPoison(2, 3).apply(context());

        // Then
        assertThat(opponent.poison()).isEqualTo(2);
    }

    @Test
    void given_no_evasion_when_grant_evasion_then_caster_has_evasion() {
        // Given / When
        new GrantEvasion(2).apply(context());

        // Then
        assertThat(caster.defenses().hasEvasion()).isTrue();
    }

    @Test
    void given_no_parry_when_gain_parry_2_then_caster_parry_is_2() {
        // Given / When
        new GainParry(2, 1).apply(context());

        // Then
        assertThat(caster.defenses().parry()).isEqualTo(2);
    }

    @Test
    void given_an_empty_board_when_summoning_two_skeletons_then_two_are_on_board() {
        // Given
        MinionTemplate skeleton = new MinionTemplate("Skeleton", 1, 1, false, new MinionAbility.None());

        // When
        new Summon(skeleton, 2).apply(context().inRound(3));

        // Then
        assertThat(caster.board().minions()).hasSize(2);
        assertThat(listener.eventsOfType(MinionSummoned.class)).hasSize(2);
    }

    @Test
    void given_a_full_board_when_summoning_then_the_summon_fizzles() {
        // Given
        MinionTemplate wolf = new MinionTemplate("Wolf", 2, 2, false);
        for (int i = 0; i < 7; i++) {
            caster.board().summon(wolf, 1);
        }

        // When
        new Summon(wolf, 1).apply(context());

        // Then
        assertThat(listener.events()).containsExactly(new SummonFizzled("P1", "Wolf"));
    }
}
