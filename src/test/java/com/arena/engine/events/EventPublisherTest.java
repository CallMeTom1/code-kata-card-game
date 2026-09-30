package com.arena.engine.events;

import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventPublisherTest {

    @Test
    void given_two_listeners_when_event_is_published_then_both_receive_it_in_order() {
        // Given
        EventPublisher publisher = new EventPublisher();
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        publisher.subscribe(first);
        publisher.subscribe(second);
        GameEvent event = new TurnStarted(1, "Alice");

        // When
        publisher.publish(event);

        // Then
        assertThat(first.received()).containsExactly(event);
        assertThat(second.received()).containsExactly(event);
    }
}
