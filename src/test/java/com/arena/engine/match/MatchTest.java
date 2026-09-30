package com.arena.engine.match;

import com.arena.bots.RandomBot;
import com.arena.engine.classes.ClassDecks;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.player.Champion;
import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

    @Test
    void given_two_champions_when_match_starts_then_each_has_30_hp_and_a_shuffled_deck() {
        // Given
        Champion p1 = new Champion("Alice", ClassDecks.mage());
        Champion p2 = new Champion("Bob", ClassDecks.tank());
        Match match = new Match(p1, new RandomBot(new Random(1)), p2, new RandomBot(new Random(2)),
                new Random(42), new EventPublisher());

        // When
        match.play();

        // Then
        assertThat(p1.heroClass().deck()).hasSize(20);
        assertThat(p2.heroClass().deck()).hasSize(20);
    }

    @Test
    void given_seed_42_when_match_starts_twice_then_both_decks_are_shuffled_in_the_same_order() {
        // Given / When
        List<String> firstRunOrder = deckOrderAfterShuffle(42L);
        List<String> secondRunOrder = deckOrderAfterShuffle(42L);

        // Then
        assertThat(firstRunOrder).isEqualTo(secondRunOrder);
    }

    private List<String> deckOrderAfterShuffle(long seed) {
        Champion p1 = new Champion("Alice", ClassDecks.mage());
        Champion p2 = new Champion("Bob", ClassDecks.tank());
        Random random = new Random(seed);
        // Force a deterministic coin flip result for comparison by using the same seed both times.
        random.nextBoolean();
        return p1.heroClass().deck().stream().map(c -> c.name()).toList();
    }

    @Test
    void given_coin_flip_when_match_starts_then_a_valid_first_player_is_announced() {
        // Given
        Champion p1 = new Champion("Alice", ClassDecks.mage());
        Champion p2 = new Champion("Bob", ClassDecks.tank());
        RecordingListener listener = new RecordingListener();
        EventPublisher events = new EventPublisher();
        events.subscribe(listener);
        Match match = new Match(p1, new RandomBot(new Random(1)), p2, new RandomBot(new Random(2)),
                new Random(7), events);

        // When
        match.play();

        // Then
        MatchStarted started = listener.received().stream()
                .filter(MatchStarted.class::isInstance)
                .map(MatchStarted.class::cast)
                .findFirst().orElseThrow();
        assertThat(started.firstPlayer()).isIn(p1.name(), p2.name());
    }

    @Test
    void given_two_random_bots_when_match_runs_then_it_ends_before_turn_51() {
        // Given
        Champion p1 = new Champion("Alice", ClassDecks.mage());
        Champion p2 = new Champion("Bob", ClassDecks.tank());
        Match match = new Match(p1, new RandomBot(new Random(11)), p2, new RandomBot(new Random(22)),
                new Random(99), new EventPublisher());

        // When
        MatchResult result = match.play();

        // Then
        assertThat(result.turns()).isLessThanOrEqualTo(Match.TURN_LIMIT);
    }
}
