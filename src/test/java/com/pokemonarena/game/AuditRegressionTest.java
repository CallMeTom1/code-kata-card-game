package com.pokemonarena.game;

import com.pokemonarena.board.PokemonInPlay;
import com.pokemonarena.bot.AggressiveBot;
import com.pokemonarena.bot.DefensiveBot;
import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroCatalog;
import com.pokemonarena.session.MatchSession;
import com.pokemonarena.session.PlayerSetup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regression tests from the healing / calculation audit. */
class AuditRegressionTest {

    private final List<MatchEvent> events = new ArrayList<>();

    private Match match(Hero hero1, List<Card> deck1, Hero hero2, List<Card> deck2) {
        return new Match(hero1, new Deck(MatchFixture.deck(deck1)), hero2, new Deck(MatchFixture.deck(deck2)),
                MatchFixture.playerOneStarts(), events::add);
    }

    private static int handIndexOf(PlayerState player, Card card) {
        int index = player.hand().indexOf(card);
        assertTrue(index >= 0, card.id() + " should be in hand");
        return index;
    }

    private <T extends MatchEvent> List<T> eventsOf(Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }

    /**
     * Reproduction of "Articuno's HP keeps increasing": every HP change of every Hero must be
     * explained by exactly one HeroDamaged / HeroHealed event, and HP must stay in [0, 30].
     */
    @Test
    void articunoControlDefensiveHpChangesAreFullyExplainedByEvents() {
        for (long seed = 1; seed <= 25; seed++) {
            int[] hp = {0, 30, 30};
            int[] healed = {0, 0, 0};
            MatchSession session = MatchSession.aiVsAi(
                    new PlayerSetup(HeroCatalog.ZAPDOS, DeckId.AGGRO), new AggressiveBot(),
                    new PlayerSetup(HeroCatalog.ARTICUNO, DeckId.CONTROL), new DefensiveBot(), seed, event -> {
                        if (event instanceof MatchEvent.HeroDamaged d) {
                            assertTrue(d.taken() >= 0 && d.taken() <= d.incoming());
                            assertEquals(hp[d.player()] - d.taken(), d.remainingHp());
                            hp[d.player()] = d.remainingHp();
                        } else if (event instanceof MatchEvent.HeroHealed h) {
                            assertTrue(h.amount() >= 0);
                            assertTrue(h.source() != null && !h.source().equals("frozen-barrier"),
                                    "Frozen Barrier must never heal");
                            assertEquals(hp[h.player()] + h.amount(), h.hp());
                            hp[h.player()] = h.hp();
                            healed[h.player()] += h.amount();
                        }
                        for (int side = 1; side <= 2; side++) {
                            assertTrue(hp[side] >= 0 && hp[side] <= 30);
                        }
                    });
            session.playToEnd();
            assertEquals(session.match().player1().currentHp(), hp[1]);
            assertEquals(session.match().player2().currentHp(), hp[2]);
        }
    }

    @Test
    void healingAtFullHpReportsZeroAndItsSource() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.POTION), HeroCatalog.ZAPDOS, List.of());
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);

        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.POTION)));

        MatchEvent.HeroHealed healed = eventsOf(MatchEvent.HeroHealed.class).getFirst();
        assertEquals(0, healed.amount());
        assertEquals(30, healed.hp());
        assertEquals("potion", healed.source());
        assertEquals(30, match.player1().currentHp());
    }

    @Test
    void attackBuffsAreConsumedByRockThrowButNotByPokemonCombat() {
        Match match = match(HeroCatalog.HO_OH, List.of(CardCatalog.PIKACHU, CardCatalog.ROCK_THROW),
                HeroCatalog.ZAPDOS, List.of(CardCatalog.SNORLAX));
        match.beginTurn();
        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.PIKACHU)));
        match.endTurn();
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.SNORLAX)));
        match.endTurn();
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.UseHeroPower()); // Sacred Flame: +2 buff

        match.perform(new Action.Attack(0, 0));
        PokemonInPlay snorlax = match.player2().board().at(0);
        assertEquals(9 - CardCatalog.PIKACHU.attack(), snorlax.currentHp(), "combat is never buffed");
        assertEquals(1, match.player1().temporaryEffects().stream()
                .filter(e -> e.kind() == TemporaryEffect.Kind.ATTACK_BUFF).count(), "combat does not consume buffs");

        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.ROCK_THROW), 0));
        assertEquals(9 - CardCatalog.PIKACHU.attack() - (3 + 2), snorlax.currentHp());
        assertTrue(match.player1().temporaryEffects().stream()
                .noneMatch(e -> e.kind() == TemporaryEffect.Kind.ATTACK_BUFF));
    }

    @Test
    void energyNeverRaisesAvailableManaAboveMaxMana() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.ENERGY), HeroCatalog.ZAPDOS, List.of());
        match.beginTurn();
        assertEquals(1, match.active().maxMana());

        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.ENERGY)));

        assertEquals(1, match.player1().maxMana());
        assertTrue(match.player1().availableMana() <= match.player1().maxMana());
    }

    @Test
    void superBonbonNeverRaisesMaxManaAboveTen() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.SUPER_BONBON), HeroCatalog.ZAPDOS, List.of());
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        assertEquals(PlayerState.MAX_MANA, match.active().maxMana());

        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.SUPER_BONBON)));

        assertEquals(PlayerState.MAX_MANA, match.player1().maxMana());
        assertTrue(match.player1().availableMana() <= PlayerState.MAX_MANA);
    }

    @Test
    void frozenBarrierExpiresAtTheEndOfTheFollowingTurnWhenUnused() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(), HeroCatalog.ZAPDOS, List.of());
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.UseHeroPower());
        match.endTurn();
        assertEquals(1, match.player1().temporaryEffects().size(), "still active during the opponent's turn");

        match.beginTurn();
        match.endTurn();
        assertTrue(match.player1().temporaryEffects().isEmpty());
        assertEquals(30, match.player1().currentHp(), "Frozen Barrier never heals");
    }

    @Test
    void frozenBarrierBlockDisplaysItsSource() {
        Match match = match(HeroCatalog.ARTICUNO, List.of(), HeroCatalog.ZAPDOS, List.of());
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.UseHeroPower());
        match.endTurn();
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.UseHeroPower()); // Static Charge: 2 damage, fully blocked

        MatchEvent.HeroDamaged hit = eventsOf(MatchEvent.HeroDamaged.class).getFirst();
        assertEquals(0, hit.taken());
        assertEquals("static-charge", hit.source());
        assertEquals(List.of("frozen-barrier"), hit.blockedBy());
    }

    @Test
    void bothPokemonDieInTheSameCombat() {
        Match match = match(HeroCatalog.ZAPDOS, List.of(CardCatalog.CHARMANDER),
                HeroCatalog.ZAPDOS, List.of(CardCatalog.CHARMANDER));
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.CHARMANDER)));
        match.endTurn();
        match.beginTurn();
        match.active().increaseMaxMana(9, 10);
        match.perform(new Action.PlayCard(handIndexOf(match.active(), CardCatalog.CHARMANDER)));
        match.endTurn();
        match.beginTurn();

        match.perform(new Action.Attack(0, 0));

        assertEquals(0, match.player1().board().size());
        assertEquals(0, match.player2().board().size());
        assertEquals(List.of(CardCatalog.CHARMANDER), match.player1().discard());
        assertEquals(List.of(CardCatalog.CHARMANDER), match.player2().discard());
        assertEquals(2, eventsOf(MatchEvent.PokemonDefeated.class).size());
        assertEquals(30, match.player1().currentHp());
        assertEquals(30, match.player2().currentHp());
    }

    private static Match playToTurnLimit(int damageToPlayer1, int damageCreditedToPlayer1) {
        Match match = MatchFixture.match(HeroCatalog.ZAPDOS, List.of(), HeroCatalog.ZAPDOS, List.of());
        match.player1().receiveDamage(damageToPlayer1);
        match.player1().recordDamageDealtToOpposingHero(damageCreditedToPlayer1);
        MatchFixture.advanceToTurn(match, Match.MAX_TURNS);
        match.endTurn();
        assertTrue(match.isOver());
        assertEquals(Match.MAX_TURNS, match.result().turns());
        return match;
    }

    @Test
    void tieBreakHighestHpWins() {
        assertEquals(MatchResult.Outcome.PLAYER_TWO_WINS, playToTurnLimit(5, 99).result().outcome());
    }

    @Test
    void tieBreakEqualHpMostDamageDealtWins() {
        assertEquals(MatchResult.Outcome.PLAYER_ONE_WINS, playToTurnLimit(0, 3).result().outcome());
    }

    @Test
    void tieBreakEqualHpAndDamageIsADraw() {
        assertEquals(MatchResult.Outcome.DRAW, playToTurnLimit(0, 0).result().outcome());
    }
}
