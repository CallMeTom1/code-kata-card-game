package com.arena.engine.combat;

import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;
import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.Test;

import static com.arena.testing.ChampionBuilder.aChampion;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleDamageResolverTest {

    @Test
    void given_a_champion_at_30_hp_when_dealt_4_damage_then_it_has_26_hp_and_damage_is_published() {
        // Given
        EventPublisher events = new EventPublisher();
        RecordingListener listener = new RecordingListener();
        events.subscribe(listener);
        Champion target = aChampion().named("P2").build();
        DamageResolver resolver = new SimpleDamageResolver(events);

        // When
        resolver.deal("Strike", target, 4);

        // Then
        assertThat(target.hp()).isEqualTo(26);
        assertThat(listener.events()).containsExactly(new DamageDealt("Strike", "P2", 4, 0, 26));
    }
}
