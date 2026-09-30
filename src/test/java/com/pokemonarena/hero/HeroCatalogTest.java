package com.pokemonarena.hero;

import com.pokemonarena.cards.CardCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeroCatalogTest {

    @Test
    void containsTheFiveDesignedHeroes() {
        assertEquals(List.of("zapdos", "articuno", "moltres", "lugia", "ho-oh"),
                HeroCatalog.all().stream().map(Hero::id).toList());
        assertEquals(List.of("Zapdos", "Articuno", "Moltres", "Lugia", "Ho-Oh"),
                HeroCatalog.all().stream().map(Hero::name).toList());
    }

    @Test
    void everyHeroStartsWithThirtyHpAndOneHeroPowerCostingTwoMana() {
        assertTrue(HeroCatalog.all().stream().allMatch(hero -> hero.startingHp() == 30));
        assertTrue(HeroCatalog.all().stream().allMatch(hero -> hero.heroPower().manaCost() == 2));
    }

    @Test
    void heroPowerIdsAreUnique() {
        assertEquals(HeroCatalog.all().size(),
                HeroCatalog.all().stream().map(hero -> hero.heroPower().id()).distinct().count());
    }

    @Test
    void staticChargeDamagesTheOpposingHero() {
        HeroPower power = HeroCatalog.ZAPDOS.heroPower();

        assertEquals(CardCategory.ATTACK, power.category());
        assertEquals("Deal 2 damage to the opposing Hero.", power.text());
    }

    @Test
    void frozenBarrierIsTheOnlyReductionWithATimedExpiry() {
        assertEquals("Reduce the next damage received by its Hero by 4 "
                + "(expires at the end of the following turn).",
                HeroCatalog.ARTICUNO.heroPower().text());
    }

    @Test
    void flameBurstHitsHarderAgainstALowHero() {
        assertEquals("Deal 3 damage to the opposing Hero, or 4 if it has 10 HP or less.",
                HeroCatalog.MOLTRES.heroPower().text());
    }

    @Test
    void aeroblastAndSacredFlameAreComposedOfSeveralEffects() {
        assertEquals("Deal 2 damage to the opposing Hero, then draw 1 card.",
                HeroCatalog.LUGIA.heroPower().text());
        assertEquals("Heal its Hero for 3 HP, then the next Attack effect deals +2 damage.",
                HeroCatalog.HO_OH.heroPower().text());
    }

    @Test
    void heroesCanBeLookedUpByTheirStableId() {
        assertSame(HeroCatalog.HO_OH, HeroCatalog.byId("ho-oh"));
        assertThrows(NoSuchElementException.class, () -> HeroCatalog.byId("mew"));
    }

    @Test
    void aHeroWithoutHeroPowerIsRejected() {
        assertThrows(NullPointerException.class, () -> new Hero("x", "X", null));
    }
}
