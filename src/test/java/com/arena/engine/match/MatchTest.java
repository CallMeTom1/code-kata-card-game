package com.arena.engine.match;

import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.effects.ApplyPoison;
import com.arena.engine.effects.DamageEqualToArmor;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.Effect;
import com.arena.engine.effects.Freeze;
import com.arena.engine.effects.GainArmor;
import com.arena.engine.effects.Summon;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.GameEvent;
import com.arena.engine.events.IllegalAction;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MinionAttacked;
import com.arena.engine.events.MulliganDone;
import com.arena.engine.events.OpeningHand;
import com.arena.engine.events.PoisonTicked;
import com.arena.engine.events.TurnStarted;
import com.arena.testing.ByNameBot;
import com.arena.testing.RecordingListener;
import com.arena.testing.ScriptedBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.arena.testing.ByNameBot.END;
import static com.arena.testing.ByNameBot.POWER;
import static com.arena.testing.TestCards.aCard;
import static com.arena.testing.TestMatches.contender;
import static com.arena.testing.TestMatches.deckOf;
import static com.arena.testing.TestMatches.p1First;
import static com.arena.testing.TestMatches.testClass;
import static org.assertj.core.api.Assertions.assertThat;

class MatchTest {

    private static final Card FILLER = aCard("Filler", 9);
    private static final Card STRIKE = new Card("Strike", 2, CardCategory.ATTACK, new DealDamage(4), false);
    private static final Card NUKE = new Card("Nuke", 1, CardCategory.ATTACK, new DealDamage(30), false);

    private EventPublisher events;
    private RecordingListener listener;

    @BeforeEach
    void setUp() {
        events = new EventPublisher();
        listener = new RecordingListener();
        events.subscribe(listener);
    }

    private Contender idle(String name) {
        return contender(name, new ScriptedBot(name), deckOf(FILLER, 20));
    }

    @Test
    void given_the_same_seed_when_two_matches_are_played_then_their_logs_are_identical() {
        // Given
        EventPublisher otherEvents = new EventPublisher();
        RecordingListener other = new RecordingListener();
        otherEvents.subscribe(other);
        List<Card> mixed = List.of(STRIKE, FILLER, NUKE, FILLER);
        List<Card> deck = java.util.stream.IntStream.range(0, 20).mapToObj(i -> mixed.get(i % 4)).toList();

        // When
        new Match(contender("P1", new ScriptedBot("P1"), deck), contender("P2", new ScriptedBot("P2"), deck),
                7L, events).play();
        new Match(contender("P1", new ScriptedBot("P1"), deck), contender("P2", new ScriptedBot("P2"), deck),
                7L, otherEvents).play();

        // Then
        assertThat(other.events()).isEqualTo(listener.events());
    }

    @Test
    void given_a_new_match_when_hands_are_dealt_then_first_has_3_cards_and_second_4_plus_the_coin() {
        // Given
        Match match = p1First(idle("P1"), idle("P2"), events);

        // When
        match.play();

        // Then
        List<OpeningHand> hands = listener.eventsOfType(OpeningHand.class);
        assertThat(hands.get(0).cards()).hasSize(3);
        assertThat(hands.get(1).cards()).hasSize(5).contains("The Coin");
    }

    @Test
    void given_a_bot_putting_back_2_cards_when_mulligan_then_it_still_has_3_cards() {
        // Given
        Bot mulliganBot = new ScriptedBot("P1") {
            @Override
            public List<Integer> mulligan(List<Card> openingHand) {
                return List.of(0, 2);
            }
        };
        Match match = p1First(contender("P1", mulliganBot, deckOf(FILLER, 20)), idle("P2"), events);

        // When
        match.play();

        // Then
        MulliganDone mulligan = listener.eventsOfType(MulliganDone.class).getFirst();
        assertThat(mulligan.putBack()).hasSize(2);
        assertThat(mulligan.drawn()).hasSize(2);
    }

    @Test
    void given_two_idle_bots_when_the_match_runs_then_fatigue_ends_it_before_turn_51() {
        // Given
        Match match = p1First(idle("P1"), idle("P2"), events);

        // When
        MatchResult result = match.play();

        // Then
        assertThat(result.reason()).isEqualTo("HP 0");
        assertThat(result.rounds()).isLessThan(50);
    }

    @Test
    void given_round_3_when_mana_phase_runs_then_the_first_player_has_3_mana() {
        // Given
        Match match = p1First(idle("P1"), idle("P2"), events);

        // When
        match.play();

        // Then
        List<ManaRefilled> p1Mana = listener.eventsOfType(ManaRefilled.class).stream()
                .filter(e -> e.player().equals("P1")).toList();
        assertThat(p1Mana.get(2).maxMana()).isEqualTo(3);
        assertThat(p1Mana.get(2).mana()).isEqualTo(3);
        assertThat(p1Mana.get(12).maxMana()).isEqualTo(10);
    }

    @Test
    void given_a_bot_playing_an_affordable_attack_when_resolved_then_the_opponent_loses_hp() {
        // Given
        Contender p1 = contender("P1", new ByNameBot("P1", END, "Strike"), deckOf(STRIKE, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(CardPlayed.class)).first()
                .isEqualTo(new CardPlayed("P1", "Strike", 2, 0));
        assertThat(listener.eventsOfType(DamageDealt.class)).first()
                .isEqualTo(new DamageDealt("Strike", "P2", 4, 0, 30, 26, 0));
    }

    @Test
    void given_a_bot_playing_an_unaffordable_card_when_asked_then_the_action_is_refused_and_the_turn_ends() {
        // Given
        Contender p1 = contender("P1", new ByNameBot("P1", "Strike"), deckOf(STRIKE, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(IllegalAction.class)).first()
                .extracting(IllegalAction::player).isEqualTo("P1");
        assertThat(listener.eventsOfType(CardPlayed.class)).isEmpty();
    }

    @Test
    void given_a_bot_using_the_hero_power_twice_when_asked_then_the_second_use_is_refused() {
        // Given
        Contender p1 = contender("P1", new ByNameBot("P1", END, END, END, POWER, POWER),
                testClass(new DealDamage(1)), deckOf(FILLER, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(DamageDealt.class)).first()
                .extracting(DamageDealt::source).isEqualTo("Test Power");
        assertThat(listener.eventsOfType(IllegalAction.class)).hasSize(1);
    }

    @Test
    void given_armor_power_then_shield_slam_played_when_resolved_then_effects_apply_in_play_order() {
        // Given
        Card slam = new Card("Slam", 1, CardCategory.ATTACK, new DamageEqualToArmor(), false);
        Contender p1 = contender("P1", new ByNameBot("P1", END, END, POWER, "Slam"),
                testClass(new GainArmor(2, 3)), deckOf(slam, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(DamageDealt.class)).first()
                .isEqualTo(new DamageDealt("Slam", "P2", 2, 0, 30, 28, 0));
    }

    @Test
    void given_the_opponent_dies_mid_queue_when_resolving_then_the_match_ends_immediately() {
        // Given
        Contender p1 = contender("P1", new ByNameBot("P1", END, END, "Nuke", "Nuke"), deckOf(NUKE, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        MatchResult result = match.play();

        // Then
        assertThat(result.winner()).isEqualTo("P1");
        assertThat(listener.eventsOfType(DamageDealt.class)).hasSize(1);
        GameEvent last = listener.events().getLast();
        assertThat(last).isInstanceOf(com.arena.engine.events.MatchEnded.class);
    }

    @Test
    void given_a_minion_summoned_on_round_1_when_round_2_resolves_then_it_hits_the_enemy_champion_once() {
        // Given
        Card wolf = new Card("Wolf Card", 1, CardCategory.ATTACK,
                new Summon(new MinionTemplate("Wolf", 2, 2, false), 1), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Wolf Card"), deckOf(wolf, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        List<MinionAttacked> attacks = listener.eventsOfType(MinionAttacked.class);
        assertThat(attacks).isNotEmpty();
        int firstAttackIndex = listener.events().indexOf(attacks.getFirst());
        long turnsBefore = listener.events().subList(0, firstAttackIndex).stream()
                .filter(TurnStarted.class::isInstance).count();
        assertThat(turnsBefore).isEqualTo(3);
    }

    @Test
    void given_an_enemy_taunt_when_a_minion_attacks_then_both_minions_trade_damage() {
        // Given
        Card wolf = new Card("Wolf Card", 1, CardCategory.ATTACK,
                new Summon(new MinionTemplate("Wolf", 2, 2, false), 1), false);
        Card wall = new Card("Wall Card", 1, CardCategory.DEFENSE,
                new Summon(new MinionTemplate("Wall", 1, 5, true), 1), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Wolf Card"), deckOf(wolf, 20));
        Contender p2 = contender("P2", new ByNameBot("P2", "Wall Card"), deckOf(wall, 20));
        Match match = p1First(p1, p2, events);

        // When
        match.play();

        // Then
        MinionAttacked firstAttack = listener.eventsOfType(MinionAttacked.class).getFirst();
        assertThat(firstAttack.target()).isEqualTo("Wall");
    }

    @Test
    void given_a_frozen_opponent_when_its_mana_phase_runs_then_it_gets_1_mana_less() {
        // Given
        Card frost = new Card("Frost", 1, CardCategory.ATTACK, new Freeze(), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Frost"), deckOf(frost, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        ManaRefilled p2FirstTurn = listener.eventsOfType(ManaRefilled.class).stream()
                .filter(e -> e.player().equals("P2")).findFirst().orElseThrow();
        assertThat(p2FirstTurn.mana()).isZero();
        assertThat(p2FirstTurn.frozen()).isTrue();
    }

    @Test
    void given_a_poisoned_opponent_when_its_turn_starts_then_it_loses_hp_through_armor() {
        // Given
        Card poison = new Card("Poison", 1, CardCategory.ATTACK, new ApplyPoison(2, 3), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Poison"), deckOf(poison, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(PoisonTicked.class)).hasSize(3)
                .allSatisfy(tick -> assertThat(tick.amount()).isEqualTo(2));
    }

    @Test
    void given_a_hero_power_effect_when_it_is_none_then_bots_can_still_use_it_for_2_mana() {
        // Given
        Contender p1 = contender("P1", new ByNameBot("P1", END, POWER), testClass(Effect.NONE), deckOf(FILLER, 20));
        Match match = p1First(p1, idle("P2"), events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(com.arena.engine.events.HeroPowerUsed.class)).hasSize(1);
    }

    @Test
    void given_an_enemy_taunt_that_would_kill_it_without_dying_when_a_minion_attacks_then_it_stays_back() {
        // Given
        Card wolf = new Card("Wolf Card", 1, CardCategory.ATTACK,
                new Summon(new MinionTemplate("Wolf", 2, 2, false), 1), false);
        Card golem = new Card("Golem Card", 1, CardCategory.DEFENSE,
                new Summon(new MinionTemplate("Golem", 2, 6, true), 1), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Wolf Card"), deckOf(wolf, 20));
        Contender p2 = contender("P2", new ByNameBot("P2", "Golem Card"), deckOf(golem, 20));
        Match match = p1First(p1, p2, events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(MinionAttacked.class)).noneMatch(a -> a.minion().equals("Wolf"));
    }

    @Test
    void given_an_enemy_taunt_when_an_attack_card_is_played_then_the_champion_is_hit_anyway() {
        // Given
        Card wall = new Card("Wall Card", 1, CardCategory.DEFENSE,
                new Summon(new MinionTemplate("Wall", 0, 5, true), 1), false);
        Contender p1 = contender("P1", new ByNameBot("P1", "Wall Card"), deckOf(wall, 20));
        Contender p2 = contender("P2", new ByNameBot("P2", END, "Strike"), deckOf(STRIKE, 20));
        Match match = p1First(p1, p2, events);

        // When
        match.play();

        // Then
        assertThat(listener.eventsOfType(DamageDealt.class)).first()
                .isEqualTo(new DamageDealt("Strike", "P1", 4, 0, 30, 26, 0));
    }
}
