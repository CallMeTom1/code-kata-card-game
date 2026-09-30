package com.arena.engine.player;

import com.arena.engine.cards.Card;
import com.arena.engine.events.CardBurned;
import com.arena.engine.events.CardDrawn;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.FatigueDamage;
import com.arena.testing.RecordingListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.arena.testing.ChampionBuilder.aChampion;
import static com.arena.testing.TestCards.aCard;
import static org.assertj.core.api.Assertions.assertThat;

class CardDrawerTest {

    private RecordingListener listener;
    private CardDrawer drawer;

    @BeforeEach
    void setUp() {
        EventPublisher events = new EventPublisher();
        listener = new RecordingListener();
        events.subscribe(listener);
        drawer = new CardDrawer(events);
    }

    @Test
    void given_a_non_empty_deck_when_drawing_then_the_top_card_goes_to_hand() {
        // Given
        Card top = aCard("Top", 1);
        Champion champion = aChampion().named("P1").withDeck(top, aCard("Next", 1)).build();

        // When
        drawer.draw(champion, 1);

        // Then
        assertThat(champion.hand()).containsExactly(top);
        assertThat(champion.deck()).hasSize(1);
        assertThat(listener.events()).containsExactly(new CardDrawn("P1", "Top"));
    }

    @Test
    void given_a_hand_of_10_when_drawing_then_the_card_is_burned() {
        // Given
        Champion champion = aChampion().named("P1").withDeck(aCard("Extra", 1)).build();
        for (int i = 0; i < 10; i++) {
            champion.addToHand(aCard("Card" + i, 1));
        }

        // When
        drawer.draw(champion, 1);

        // Then
        assertThat(champion.hand()).hasSize(10);
        assertThat(listener.events()).containsExactly(new CardBurned("P1", "Extra"));
    }

    @Test
    void given_an_empty_deck_when_drawing_3_times_then_fatigue_deals_1_then_2_then_3() {
        // Given
        Champion champion = aChampion().named("P1").build();

        // When
        drawer.draw(champion, 3);

        // Then
        assertThat(champion.hp()).isEqualTo(24);
        assertThat(listener.eventsOfType(FatigueDamage.class)).extracting(FatigueDamage::amount)
                .containsExactly(1, 2, 3);
    }
}
