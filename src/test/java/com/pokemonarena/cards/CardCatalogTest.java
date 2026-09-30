package com.pokemonarena.cards;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardCatalogTest {

    @Test
    void containsTheThirteenDesignedPokemonAndTwelveItems() {
        assertEquals(13, CardCatalog.pokemon().size());
        assertEquals(12, CardCatalog.items().size());
        assertEquals(25, CardCatalog.size());
    }

    @Test
    void cardIdsAreUniqueStableLowercaseTechnicalIdentifiers() {
        Set<String> ids = CardCatalog.all().stream().map(Card::id).collect(Collectors.toSet());

        assertEquals(CardCatalog.size(), ids.size());
        assertTrue(ids.stream().allMatch(id -> id.matches("[a-z0-9-]+")), ids::toString);
    }

    @Test
    void pokemonIdsAreAlignedWithPokeApiSlugs() {
        assertEquals("charmander", CardCatalog.CHARMANDER.id());
        assertEquals("Charmander", CardCatalog.CHARMANDER.name());
        assertEquals("squirtle", CardCatalog.SQUIRTLE.id());
        assertEquals("chansey", CardCatalog.CHANSEY.id());
        assertEquals("pidgey", CardCatalog.PIDGEY.id());
    }

    @Test
    void pokemonKeepTheirDesignedStats() {
        assertEquals(1, CardCatalog.PIKACHU.manaCost());
        assertEquals(2, CardCatalog.PIKACHU.attack());
        assertEquals(2, CardCatalog.PIKACHU.maxHp());

        assertEquals(7, CardCatalog.CHARIZARD.manaCost());
        assertEquals(8, CardCatalog.CHARIZARD.attack());
        assertEquals(8, CardCatalog.CHARIZARD.maxHp());

        assertEquals(7, CardCatalog.BLASTOISE.manaCost());
        assertEquals(6, CardCatalog.BLASTOISE.attack());
        assertEquals(10, CardCatalog.BLASTOISE.maxHp());
    }

    @Test
    void naturesMatchTheCardType() {
        assertTrue(CardCatalog.pokemon().stream().allMatch(card -> card.nature() == CardNature.POKEMON));
        assertTrue(CardCatalog.items().stream().allMatch(card -> card.nature() == CardNature.ITEM));
    }

    @Test
    void onlyTheFivePokemonWithAnAbilityHaveAWhenPlayedEffect() {
        Set<String> withAbility = CardCatalog.pokemon().stream()
                .filter(card -> card.ability().isPresent())
                .map(PokemonCard::id)
                .collect(Collectors.toSet());

        assertEquals(Set.of("pidgey", "onix", "eevee", "chansey", "abra"), withAbility);
        assertFalse(CardCatalog.PIKACHU.ability().isPresent());
    }

    @Test
    void attackItemsMakeTheAttackCategoryMeaningful() {
        assertEquals(CardCategory.ATTACK, CardCatalog.FIRE_BLAST.category());
        assertEquals(3, CardCatalog.FIRE_BLAST.manaCost());
        assertEquals(CardCategory.ATTACK, CardCatalog.THUNDER_SHOCK.category());
        assertEquals(2, CardCatalog.THUNDER_SHOCK.manaCost());
        assertEquals(CardCategory.ATTACK, CardCatalog.ROCK_THROW.category());
        assertTrue(CardCatalog.ROCK_THROW.requiresPokemonTarget());
        assertFalse(CardCatalog.FIRE_BLAST.requiresPokemonTarget());
    }

    @Test
    void everyGameplayCategoryIsRepresented() {
        Set<CardCategory> categories = CardCatalog.all().stream()
                .map(Card::category)
                .collect(Collectors.toSet());

        assertEquals(Set.of(CardCategory.values()), categories);
    }

    @Test
    void cardsCanBeLookedUpByTheirStableId() {
        assertSame(CardCatalog.ONIX, CardCatalog.byId("onix"));
        assertSame(CardCatalog.POKE_BALL, CardCatalog.byId("poke-ball"));
        assertThrows(NoSuchElementException.class, () -> CardCatalog.byId("mewtwo"));
    }

    @Test
    void cardTextDescribesTheEffect() {
        assertEquals("Heal its Hero for 5 HP.", CardCatalog.POTION.text());
        assertEquals("When played: reduce the next damage received by its Hero by 2.",
                CardCatalog.ONIX.text());
        assertEquals("", CardCatalog.PIKACHU.text());
    }

    @Test
    void invalidCardDefinitionsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new PokemonCard("x", "X", 1, CardCategory.ATTACK, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new PokemonCard("x", "X", -1, CardCategory.ATTACK, 1, 1));
        assertThrows(NullPointerException.class,
                () -> new ItemCard("x", "X", 1, CardCategory.UTILITY, null));
    }
}
