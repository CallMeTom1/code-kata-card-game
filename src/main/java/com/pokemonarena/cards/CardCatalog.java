package com.pokemonarena.cards;

import com.pokemonarena.cards.effects.AttackBuffEffect;
import com.pokemonarena.cards.effects.DamageHeroEffect;
import com.pokemonarena.cards.effects.DamagePokemonEffect;
import com.pokemonarena.cards.effects.DamageReductionEffect;
import com.pokemonarena.cards.effects.DrawEffect;
import com.pokemonarena.cards.effects.HealHeroEffect;
import com.pokemonarena.cards.effects.IncreaseMaxManaEffect;
import com.pokemonarena.cards.effects.RestoreManaEffect;
import com.pokemonarena.cards.effects.ReturnPokemonFromDiscardEffect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The predefined card pool. Ids are stable lowercase technical identifiers (PokéAPI slugs for
 * Pokémon); display names are English and are never used as identifiers. Localization is a
 * frontend concern and stays out of the engine.
 */
public final class CardCatalog {

    // Pokémon
    public static final PokemonCard PIKACHU =
            new PokemonCard("pikachu", "Pikachu", 1, CardCategory.ATTACK, 2, 2);
    public static final PokemonCard CHARMANDER =
            new PokemonCard("charmander", "Charmander", 2, CardCategory.ATTACK, 3, 3);
    public static final PokemonCard SQUIRTLE =
            new PokemonCard("squirtle", "Squirtle", 2, CardCategory.DEFENSE, 1, 5);
    public static final PokemonCard BULBASAUR =
            new PokemonCard("bulbasaur", "Bulbasaur", 3, CardCategory.ATTACK, 3, 4);
    public static final PokemonCard PIDGEY =
            new PokemonCard("pidgey", "Pidgey", 2, CardCategory.ATTACK, 2, 2,
                    new AttackBuffEffect(1, true));
    public static final PokemonCard CHARMELEON =
            new PokemonCard("charmeleon", "Charmeleon", 4, CardCategory.ATTACK, 5, 5);
    public static final PokemonCard CHARIZARD =
            new PokemonCard("charizard", "Charizard", 7, CardCategory.ATTACK, 8, 8);
    public static final PokemonCard BLASTOISE =
            new PokemonCard("blastoise", "Blastoise", 7, CardCategory.DEFENSE, 6, 10);
    public static final PokemonCard ONIX =
            new PokemonCard("onix", "Onix", 3, CardCategory.DEFENSE, 2, 6,
                    new DamageReductionEffect(2));
    public static final PokemonCard SNORLAX =
            new PokemonCard("snorlax", "Snorlax", 5, CardCategory.DEFENSE, 3, 9);
    public static final PokemonCard EEVEE =
            new PokemonCard("eevee", "Eevee", 2, CardCategory.UTILITY, 2, 3,
                    new DrawEffect(1));
    public static final PokemonCard CHANSEY =
            new PokemonCard("chansey", "Chansey", 4, CardCategory.UTILITY, 1, 7,
                    new HealHeroEffect(5));
    public static final PokemonCard ABRA =
            new PokemonCard("abra", "Abra", 3, CardCategory.ATTACK, 2, 3,
                    new AttackBuffEffect(3));

    // Items
    public static final ItemCard POTION =
            new ItemCard("potion", "Potion", 2, CardCategory.UTILITY, new HealHeroEffect(5));
    public static final ItemCard SUPER_POTION =
            new ItemCard("super-potion", "Super Potion", 3, CardCategory.UTILITY, new HealHeroEffect(8));
    public static final ItemCard HYPER_POTION =
            new ItemCard("hyper-potion", "Hyper Potion", 5, CardCategory.UTILITY, new HealHeroEffect(12));
    public static final ItemCard POKE_BALL =
            new ItemCard("poke-ball", "Poké Ball", 2, CardCategory.UTILITY, new DrawEffect(2));
    public static final ItemCard RAPPEL =
            new ItemCard("rappel", "Rappel", 4, CardCategory.UTILITY, new ReturnPokemonFromDiscardEffect(),
                    ItemCard.Target.DISCARDED_POKEMON);
    public static final ItemCard SUPER_BONBON =
            new ItemCard("super-bonbon", "Super Bonbon", 2, CardCategory.RESOURCE, new IncreaseMaxManaEffect(1, 1));
    public static final ItemCard ENERGY =
            new ItemCard("energy", "Energy", 1, CardCategory.RESOURCE, new RestoreManaEffect(2));
    public static final ItemCard DEFENSE_X =
            new ItemCard("defense-x", "Defense X", 2, CardCategory.DEFENSE, new DamageReductionEffect(4));
    public static final ItemCard PROTECTION =
            new ItemCard("protection", "Protection", 4, CardCategory.DEFENSE, new DamageReductionEffect(7));
    public static final ItemCard FIRE_BLAST =
            new ItemCard("fire-blast", "Fire Blast", 3, CardCategory.ATTACK, new DamageHeroEffect(4));
    public static final ItemCard THUNDER_SHOCK =
            new ItemCard("thunder-shock", "Thunder Shock", 2, CardCategory.ATTACK, new DamageHeroEffect(2));
    public static final ItemCard ROCK_THROW =
            new ItemCard("rock-throw", "Rock Throw", 2, CardCategory.ATTACK, new DamagePokemonEffect(3),
                    ItemCard.Target.OPPOSING_POKEMON);

    private static final List<PokemonCard> POKEMON = List.of(
            PIKACHU, CHARMANDER, SQUIRTLE, BULBASAUR, PIDGEY, CHARMELEON, CHARIZARD,
            BLASTOISE, ONIX, SNORLAX, EEVEE, CHANSEY, ABRA);

    private static final List<ItemCard> ITEMS = List.of(
            POTION, SUPER_POTION, HYPER_POTION, POKE_BALL, RAPPEL, SUPER_BONBON, ENERGY,
            DEFENSE_X, PROTECTION, FIRE_BLAST, THUNDER_SHOCK, ROCK_THROW);

    private static final Map<String, Card> BY_ID = indexById();

    private CardCatalog() {
    }

    public static List<PokemonCard> pokemon() {
        return POKEMON;
    }

    public static List<ItemCard> items() {
        return ITEMS;
    }

    /** Every card of the pool, Pokémon first. */
    public static List<Card> all() {
        return List.copyOf(BY_ID.values());
    }

    /** Number of distinct cards in the pool. */
    public static int size() {
        return BY_ID.size();
    }

    public static Card byId(String id) {
        Card card = BY_ID.get(id);
        if (card == null) {
            throw new NoSuchElementException("unknown card id: " + id);
        }
        return card;
    }

    private static Map<String, Card> indexById() {
        Map<String, Card> byId = new LinkedHashMap<>();
        for (Card card : POKEMON) {
            register(byId, card);
        }
        for (Card card : ITEMS) {
            register(byId, card);
        }
        return Collections.unmodifiableMap(byId);
    }

    private static void register(Map<String, Card> byId, Card card) {
        if (byId.put(card.id(), card) != null) {
            throw new IllegalStateException("duplicate card id: " + card.id());
        }
    }
}
