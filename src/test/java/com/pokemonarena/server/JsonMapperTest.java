package com.pokemonarena.server;

import com.pokemonarena.bot.AggressiveBot;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;
import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.game.MatchResult;
import com.pokemonarena.game.TemporaryEffect;
import com.pokemonarena.hero.HeroCatalog;
import com.pokemonarena.session.MatchSession;
import com.pokemonarena.session.PlayerSetup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonMapperTest {

    @SuppressWarnings("unchecked")
    private static Map<String, Object> roundTrip(Object value) {
        return (Map<String, Object>) Json.parse(Json.write(value));
    }

    @Test
    void catalogComesFromTheJavaCatalog() {
        Map<String, Object> catalog = roundTrip(JsonMapper.catalog());

        List<?> cards = (List<?>) catalog.get("cards");
        assertEquals(CardCatalog.size(), cards.size());
        Map<?, ?> pikachu = (Map<?, ?>) cards.get(0);
        assertEquals("pikachu", pikachu.get("id"));
        assertEquals((long) CardCatalog.PIKACHU.attack(), pikachu.get("attack"));
        assertEquals((long) CardCatalog.PIKACHU.maxHp(), pikachu.get("maxHp"));
        assertEquals("POKEMON", pikachu.get("nature"));
        Map<?, ?> rockThrow = cards.stream().map(c -> (Map<?, ?>) c)
                .filter(c -> c.get("id").equals("rock-throw")).findFirst().orElseThrow();
        assertEquals("OPPOSING_POKEMON", rockThrow.get("target"));
        assertEquals(CardCatalog.ROCK_THROW.text(), rockThrow.get("text"));

        List<?> heroes = (List<?>) catalog.get("heroes");
        assertEquals(HeroCatalog.all().size(), heroes.size());
        Map<?, ?> power = (Map<?, ?>) ((Map<?, ?>) heroes.get(0)).get("power");
        assertEquals("static-charge", power.get("id"));
        assertEquals(2L, power.get("manaCost"));
        assertEquals(DeckId.values().length, ((List<?>) catalog.get("decks")).size());
        assertEquals(List.of("Aggressive", "Defensive"), catalog.get("bots"));
    }

    @Test
    void actionsRoundTrip() {
        for (Action action : List.of(new Action.PlayCard(2, 1), new Action.PlayCard(0), new Action.UseHeroPower(),
                new Action.Attack(1, 0), Action.Attack.onHero(2), new Action.EndTurn())) {
            assertEquals(action, JsonMapper.toAction(roundTrip(JsonMapper.action(action))));
        }
        assertEquals(new Action.PlayCard(3), JsonMapper.toAction(roundTrip(Map.of("type", "PlayCard", "handIndex", 3))));
    }

    @Test
    void badActionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> JsonMapper.toAction(Map.of("type", "Cheat")));
        assertThrows(IllegalArgumentException.class, () -> JsonMapper.toAction(Map.of("type", "PlayCard")));
        assertThrows(IllegalArgumentException.class, () -> JsonMapper.toAction(Map.of()));
    }

    @Test
    void viewHidesTheOpponentHandAndMatchesTheEngine() {
        MatchSession session = MatchSession.humanVsAi(new PlayerSetup(HeroCatalog.LUGIA, DeckId.BALANCED),
                new PlayerSetup(HeroCatalog.MOLTRES, DeckId.AGGRO), new AggressiveBot(), 3, e -> { });
        session.advance();
        GameView view = GameView.forSide(session.match(), 1);

        Map<String, Object> json = roundTrip(JsonMapper.view(view));

        Map<?, ?> self = (Map<?, ?>) json.get("self");
        Map<?, ?> opponent = (Map<?, ?>) json.get("opponent");
        assertEquals("lugia", self.get("heroId"));
        assertEquals((long) view.self().currentHp(), self.get("currentHp"));
        assertEquals(view.self().hand().size(), ((List<?>) self.get("hand")).size());
        assertEquals("moltres", opponent.get("heroId"));
        assertFalse(opponent.containsKey("hand"));
        assertEquals((long) view.opponent().hand().size(), opponent.get("handSize"));
        assertEquals(view.legalActions().size(), ((List<?>) json.get("legalActions")).size());
        assertEquals("PLAY", json.get("phase"));
    }

    @Test
    void eventsKeepTheirTypeAndFields() {
        Map<String, Object> damaged = roundTrip(JsonMapper.event(
                new MatchEvent.HeroDamaged(2, 5, 3, 27, "pikachu", List.of("onix"))));
        assertEquals(Map.of("type", "HeroDamaged", "player", 2L, "incoming", 5L, "taken", 3L, "remainingHp", 27L,
                "source", "pikachu", "blockedBy", List.of("onix")), damaged);

        Map<String, Object> healed = roundTrip(JsonMapper.event(new MatchEvent.HeroHealed(1, 0, 30, "potion")));
        assertEquals("potion", healed.get("source"));

        Map<String, Object> attack = roundTrip(JsonMapper.event(new MatchEvent.AttackDeclared(1, "pikachu", null)));
        assertTrue(attack.containsKey("targetId"));
        assertNull(attack.get("targetId"));

        Map<String, Object> queued = roundTrip(JsonMapper.event(new MatchEvent.EffectQueued(1,
                TemporaryEffect.damageReduction("defense-x", 4, TemporaryEffect.NEVER_EXPIRES))));
        Map<?, ?> effect = (Map<?, ?>) queued.get("effect");
        assertEquals("DAMAGE_REDUCTION", effect.get("kind"));
        assertNull(effect.get("expiresAfterTurn"));

        Map<String, Object> ended = roundTrip(JsonMapper.event(new MatchEvent.MatchEnded(
                new MatchResult(MatchResult.Outcome.DRAW, 50, "tie"))));
        assertEquals("DRAW", ((Map<?, ?>) ended.get("result")).get("outcome"));
    }

    @Test
    void setupsAndSeedsAreRead() {
        PlayerSetup setup = JsonMapper.toSetup(Map.of("h", "ho-oh", "d", "energy"), "h", "d");
        assertEquals(new PlayerSetup(HeroCatalog.HO_OH, DeckId.ENERGY), setup);
        assertThrows(RuntimeException.class, () -> JsonMapper.toSetup(Map.of("h", "mew", "d", "AGGRO"), "h", "d"));
        assertEquals(12L, JsonMapper.optionalLong(Map.of("seed", "12"), "seed"));
        assertEquals(12L, JsonMapper.optionalLong(Map.of("seed", 12L), "seed"));
        assertNull(JsonMapper.optionalLong(Map.of(), "seed"));
        assertNull(JsonMapper.optionalLong(Map.of("seed", ""), "seed"));
    }
}
