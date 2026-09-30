package com.arena.engine.events;

import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class EventPublisherTest {

    @Test
    void given_two_listeners_when_an_event_is_published_then_both_receive_it() {
        // Given
        EventPublisher publisher = new EventPublisher();
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        publisher.subscribe(first);
        publisher.subscribe(second);
        GameEvent event = new TurnStarted(1, "P1");

        // When
        publisher.publish(event);

        // Then
        assertThat(first.events()).containsExactly(event);
        assertThat(second.events()).containsExactly(event);
    }

    @Test
    void given_several_events_when_published_then_listeners_receive_them_in_order() {
        // Given
        EventPublisher publisher = new EventPublisher();
        RecordingListener listener = new RecordingListener();
        publisher.subscribe(listener);
        GameEvent started = new MatchStarted("P1", "P2", 42L, "P1");
        GameEvent played = new CardPlayed("P1", "Strike", 2, 0);
        GameEvent ended = new MatchEnded("P1", "HP 0", 7);

        // When
        publisher.publish(started);
        publisher.publish(played);
        publisher.publish(ended);

        // Then
        assertThat(listener.events()).containsExactly(started, played, ended);
    }

    @Test
    void given_no_listener_when_an_event_is_published_then_nothing_fails() {
        // Given
        EventPublisher publisher = new EventPublisher();

        // When / Then
        assertThatCode(() -> publisher.publish(new DamageDealt("Strike", "P2", 4, 0, 26)))
                .doesNotThrowAnyException();
    }
}
