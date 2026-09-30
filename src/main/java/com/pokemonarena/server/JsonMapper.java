package com.pokemonarena.server;

import com.pokemonarena.board.Board;
import com.pokemonarena.bot.Bots;
import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.cards.ItemCard;
import com.pokemonarena.cards.PokemonCard;
import com.pokemonarena.deck.DeckId;
import com.pokemonarena.deck.DeckLists;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;
import com.pokemonarena.game.Match;
import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.game.MatchResult;
import com.pokemonarena.game.PlayerState;
import com.pokemonarena.game.PlayerView;
import com.pokemonarena.game.PokemonView;
import com.pokemonarena.game.TemporaryEffect;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroCatalog;
import com.pokemonarena.hero.HeroPower;
import com.pokemonarena.session.PlayerSetup;
import com.pokemonarena.simulation.SimulationStats;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts engine objects to JSON-ready maps/lists and request JSON to engine objects. Pure
 * translation: it contains no gameplay rule and every value comes from the engine.
 */
public final class JsonMapper {

    private JsonMapper() {
    }

    // --- catalog ---------------------------------------------------------------------------

    public static Map<String, Object> catalog() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("cards", CardCatalog.all().stream().map(JsonMapper::card).toList());
        json.put("heroes", HeroCatalog.all().stream().map(JsonMapper::hero).toList());
        json.put("decks", List.of(DeckId.values()).stream().map(id -> Map.of(
                "id", id.name(),
                "cards", DeckLists.cards(id).stream().map(Card::id).toList())).toList());
        json.put("bots", Bots.names());
        json.put("boardSize", Board.MAX_POKEMON);
        json.put("maxMana", PlayerState.MAX_MANA);
        json.put("maxTurns", Match.MAX_TURNS);
        return json;
    }

    static Map<String, Object> card(Card card) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", card.id());
        json.put("name", card.name());
        json.put("nature", card.nature());
        json.put("category", card.category());
        json.put("manaCost", card.manaCost());
        json.put("text", card.text());
        if (card instanceof PokemonCard pokemon) {
            json.put("attack", pokemon.attack());
            json.put("maxHp", pokemon.maxHp());
        } else if (card instanceof ItemCard item) {
            json.put("target", item.target());
        }
        return json;
    }

    static Map<String, Object> hero(Hero hero) {
        HeroPower power = hero.heroPower();
        Map<String, Object> powerJson = new LinkedHashMap<>();
        powerJson.put("id", power.id());
        powerJson.put("name", power.name());
        powerJson.put("manaCost", power.manaCost());
        powerJson.put("category", power.category());
        powerJson.put("text", power.text());
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", hero.id());
        json.put("name", hero.name());
        json.put("maxHp", hero.startingHp());
        json.put("power", powerJson);
        return json;
    }

    // --- view ------------------------------------------------------------------------------

    /** The view as JSON; the opponent's hand is reduced to its size (hidden information). */
    public static Map<String, Object> view(GameView view) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("turnNumber", view.turnNumber());
        json.put("phase", view.phase());
        json.put("activeSide", view.activeSide());
        json.put("over", view.isOver());
        json.put("self", player(view.self(), true));
        json.put("opponent", player(view.opponent(), false));
        json.put("legalActions", view.legalActions().stream().map(JsonMapper::action).toList());
        return json;
    }

    static Map<String, Object> player(PlayerView player, boolean showHand) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("side", player.side());
        json.put("heroId", player.hero().id());
        json.put("currentHp", player.currentHp());
        json.put("maxHp", player.maxHp());
        json.put("availableMana", player.availableMana());
        json.put("maxMana", player.maxMana());
        json.put("heroPowerUsedThisTurn", player.heroPowerUsedThisTurn());
        json.put("handSize", player.hand().size());
        if (showHand) {
            json.put("hand", player.hand().stream().map(Card::id).toList());
        }
        json.put("board", player.board().stream().map(JsonMapper::pokemon).toList());
        json.put("deckCount", player.deckCount());
        json.put("discard", player.discard().stream().map(Card::id).toList());
        json.put("temporaryEffects", player.temporaryEffects().stream().map(JsonMapper::effect).toList());
        json.put("damageDealtToOpposingHero", player.damageDealtToOpposingHero());
        return json;
    }

    static Map<String, Object> pokemon(PokemonView pokemon) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("cardId", pokemon.id());
        json.put("attack", pokemon.attack());
        json.put("currentHp", pokemon.currentHp());
        json.put("maxHp", pokemon.maxHp());
        json.put("canAttack", pokemon.canAttack());
        return json;
    }

    static Map<String, Object> effect(TemporaryEffect effect) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("kind", effect.kind());
        json.put("source", effect.source());
        json.put("value", effect.value());
        json.put("expiresAfterTurn",
                effect.expiresAfterTurn() == TemporaryEffect.NEVER_EXPIRES ? null : effect.expiresAfterTurn());
        return json;
    }

    public static Map<String, Object> result(MatchResult result) {
        if (result == null) {
            return null;
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("outcome", result.outcome());
        json.put("turns", result.turns());
        json.put("reason", result.reason());
        return json;
    }

    // --- events ----------------------------------------------------------------------------

    /** An event as {@code {"type": "<RecordName>", <record components>...}}. */
    public static Map<String, Object> event(MatchEvent event) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", event.getClass().getSimpleName());
        for (RecordComponent component : event.getClass().getRecordComponents()) {
            Object value;
            try {
                value = component.getAccessor().invoke(event);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("cannot read " + component.getName(), e);
            }
            if (value instanceof TemporaryEffect effect) {
                value = effect(effect);
            } else if (value instanceof MatchResult result) {
                value = result(result);
            }
            json.put(component.getName(), value);
        }
        return json;
    }

    public static List<Map<String, Object>> events(List<MatchEvent> events) {
        return events.stream().map(JsonMapper::event).toList();
    }

    // --- actions ---------------------------------------------------------------------------

    public static Map<String, Object> action(Action action) {
        Map<String, Object> json = new LinkedHashMap<>();
        switch (action) {
            case Action.PlayCard play -> {
                json.put("type", "PlayCard");
                json.put("handIndex", play.handIndex());
                json.put("targetIndex", play.targetIndex());
            }
            case Action.UseHeroPower ignored -> json.put("type", "UseHeroPower");
            case Action.Attack attack -> {
                json.put("type", "Attack");
                json.put("attackerIndex", attack.attackerIndex());
                json.put("targetIndex", attack.targetIndex());
            }
            case Action.EndTurn ignored -> json.put("type", "EndTurn");
        }
        return json;
    }

    public static Action toAction(Map<String, Object> json) {
        String type = string(json, "type");
        return switch (type) {
            case "PlayCard" -> new Action.PlayCard(integer(json, "handIndex"),
                    optionalInteger(json, "targetIndex", Action.NO_POKEMON_TARGET));
            case "UseHeroPower" -> new Action.UseHeroPower();
            case "Attack" -> new Action.Attack(integer(json, "attackerIndex"),
                    optionalInteger(json, "targetIndex", Action.NO_POKEMON_TARGET));
            case "EndTurn" -> new Action.EndTurn();
            default -> throw new IllegalArgumentException("unknown action type: " + type);
        };
    }

    // --- setups & stats --------------------------------------------------------------------

    /** Reads {@code {"<heroKey>": "zapdos", "<deckKey>": "AGGRO"}}. */
    public static PlayerSetup toSetup(Map<String, Object> json, String heroKey, String deckKey) {
        return new PlayerSetup(HeroCatalog.byId(string(json, heroKey)),
                DeckId.valueOf(string(json, deckKey).toUpperCase(Locale.ROOT)));
    }

    public static Map<String, Object> stats(SimulationStats stats) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("matches", stats.matches());
        json.put("winsSide1", stats.winsSide1());
        json.put("winsSide2", stats.winsSide2());
        json.put("draws", stats.draws());
        json.put("winRateSide1", stats.winRateSide1());
        json.put("winRateSide2", stats.winRateSide2());
        json.put("drawRate", stats.drawRate());
        json.put("averageTurns", stats.averageTurns());
        json.put("averageDamageSide1", stats.averageDamageSide1());
        json.put("averageDamageSide2", stats.averageDamageSide2());
        return json;
    }

    // --- request helpers -------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object json) {
        if (json instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("a JSON object is expected");
    }

    public static String string(Map<String, Object> json, String key) {
        if (json.get(key) instanceof String value) {
            return value;
        }
        throw new IllegalArgumentException("missing string field: " + key);
    }

    public static int integer(Map<String, Object> json, String key) {
        if (json.get(key) instanceof Long value) {
            return Math.toIntExact(value);
        }
        throw new IllegalArgumentException("missing integer field: " + key);
    }

    public static int optionalInteger(Map<String, Object> json, String key, int fallback) {
        return json.get(key) == null ? fallback : integer(json, key);
    }

    /** An optional {@code long} field (e.g. a seed), or {@code null} when absent. */
    public static Long optionalLong(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("field must be an integer: " + key);
            }
        }
        if (value instanceof String) {
            return null;
        }
        throw new IllegalArgumentException("field must be an integer: " + key);
    }
}
