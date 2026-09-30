package com.arena.engine.combat;

import com.arena.engine.board.Minion;
import com.arena.engine.board.MinionTemplate;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EvasionTriggered;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.MinionDamaged;
import com.arena.engine.events.MinionDied;
import com.arena.engine.player.Champion;
import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.arena.testing.ChampionBuilder.aChampion;
import static org.assertj.core.api.Assertions.assertThat;

class CombatTest {

    private RecordingListener listener;
    private Combat combat;

    @BeforeEach
    void setUp() {
        EventPublisher events = new EventPublisher();
        listener = new RecordingListener();
        events.subscribe(listener);
        combat = new Combat(events);
    }

    @Test
    void given_no_defense_when_dealt_4_damage_then_hp_drops_by_4_and_damage_is_published() {
        // Given
        Champion target = aChampion().named("P2").build();

        // When
        combat.deal("Strike", target, 4);

        // Then
        assertThat(target.hp()).isEqualTo(26);
        assertThat(listener.events()).containsExactly(new DamageDealt("Strike", "P2", 4, 0, 30, 26));
    }

    @Test
    void given_armor_3_when_dealt_4_damage_then_armor_absorbs_first() {
        // Given
        Champion target = aChampion().named("P2").build();
        target.defenses().addArmor(3, 2);

        // When
        combat.deal("Strike", target, 4);

        // Then
        assertThat(target.hp()).isEqualTo(29);
        assertThat(target.defenses().armor()).isZero();
        assertThat(listener.eventsOfType(DamageDealt.class)).first()
                .extracting(DamageDealt::absorbed).isEqualTo(3);
    }

    @Test
    void given_parry_2_when_hit_for_3_then_1_damage_passes() {
        // Given
        Champion target = aChampion().build();
        target.defenses().addParry(2, 1);

        // When
        combat.deal("Twin Blades", target, 3);

        // Then
        assertThat(target.hp()).isEqualTo(29);
    }

    @Test
    void given_armor_when_damage_ignores_armor_then_hp_is_hit_directly() {
        // Given
        Champion target = aChampion().build();
        target.defenses().addArmor(5, 2);

        // When
        combat.dealIgnoringArmor("Eviscerate", target, 3);

        // Then
        assertThat(target.hp()).isEqualTo(27);
        assertThat(target.defenses().armor()).isEqualTo(5);
    }

    @Test
    void given_evasion_when_dealt_damage_twice_then_only_the_first_hit_is_prevented() {
        // Given
        Champion target = aChampion().named("P2").build();
        target.defenses().grantEvasion(2);

        // When
        combat.deal("Fireball", target, 6);
        combat.deal("Strike", target, 4);

        // Then
        assertThat(target.hp()).isEqualTo(26);
        assertThat(listener.eventsOfType(EvasionTriggered.class))
                .containsExactly(new EvasionTriggered("P2", "Fireball", 6));
    }

    @Test
    void given_a_1_1_minion_when_dealt_2_damage_then_it_dies_and_leaves_the_board() {
        // Given
        Champion owner = aChampion().named("P1").build();
        Minion skeleton = owner.board().summon(new MinionTemplate("Skeleton", 1, 1, false), 1).orElseThrow();

        // When
        combat.damageMinion("Whirlwind Slash", owner, skeleton, 2);

        // Then
        assertThat(owner.board().minions()).isEmpty();
        assertThat(listener.eventsOfType(MinionDamaged.class)).hasSize(1);
        assertThat(listener.eventsOfType(MinionDied.class)).containsExactly(new MinionDied("P1", "Skeleton"));
    }
}
