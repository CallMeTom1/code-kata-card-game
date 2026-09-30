package com.pokemonarena.hero;

import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.cards.effects.AttackBuffEffect;
import com.pokemonarena.cards.effects.CompositeEffect;
import com.pokemonarena.cards.effects.DamageHeroEffect;
import com.pokemonarena.cards.effects.DamageReductionEffect;
import com.pokemonarena.cards.effects.DrawEffect;
import com.pokemonarena.cards.effects.HealHeroEffect;
import com.pokemonarena.cards.effects.ThresholdDamageHeroEffect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The five predefined Heroes and their Hero Powers. Ids are stable lowercase PokéAPI slugs and
 * display names are English; localization is a frontend concern.
 */
public final class HeroCatalog {

    public static final Hero ZAPDOS = new Hero("zapdos", "Zapdos",
            new HeroPower("static-charge", "Static Charge", 2, CardCategory.ATTACK,
                    new DamageHeroEffect(2)));

    public static final Hero ARTICUNO = new Hero("articuno", "Articuno",
            new HeroPower("frozen-barrier", "Frozen Barrier", 2, CardCategory.DEFENSE,
                    new DamageReductionEffect(4, true)));

    public static final Hero MOLTRES = new Hero("moltres", "Moltres",
            new HeroPower("flame-burst", "Flame Burst", 2, CardCategory.ATTACK,
                    new ThresholdDamageHeroEffect(3, 10, 4)));

    public static final Hero LUGIA = new Hero("lugia", "Lugia",
            new HeroPower("aeroblast", "Aeroblast", 2, CardCategory.ATTACK,
                    new CompositeEffect(new DamageHeroEffect(2), new DrawEffect(1))));

    public static final Hero HO_OH = new Hero("ho-oh", "Ho-Oh",
            new HeroPower("sacred-flame", "Sacred Flame", 2, CardCategory.UTILITY,
                    new CompositeEffect(new HealHeroEffect(3), new AttackBuffEffect(2))));

    private static final List<Hero> HEROES = List.of(ZAPDOS, ARTICUNO, MOLTRES, LUGIA, HO_OH);

    private static final Map<String, Hero> BY_ID = indexById();

    private HeroCatalog() {
    }

    public static List<Hero> all() {
        return HEROES;
    }

    public static Hero byId(String id) {
        Hero hero = BY_ID.get(id);
        if (hero == null) {
            throw new NoSuchElementException("unknown hero id: " + id);
        }
        return hero;
    }

    private static Map<String, Hero> indexById() {
        Map<String, Hero> byId = new LinkedHashMap<>();
        for (Hero hero : HEROES) {
            if (byId.put(hero.id(), hero) != null) {
                throw new IllegalStateException("duplicate hero id: " + hero.id());
            }
        }
        return Collections.unmodifiableMap(byId);
    }
}
