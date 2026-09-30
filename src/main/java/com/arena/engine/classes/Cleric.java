package com.arena.engine.classes;

import com.arena.engine.board.MinionAbility;
import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.effects.DamageChampionAndAllEnemyMinions;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.DrawCards;
import com.arena.engine.effects.GainArmor;
import com.arena.engine.effects.GainMaxMana;
import com.arena.engine.effects.Heal;
import com.arena.engine.effects.Summon;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.ATTACK;
import static com.arena.engine.cards.CardCategory.DEFENSE;
import static com.arena.engine.cards.CardCategory.RESOURCE;
import static com.arena.engine.cards.CardCategory.UTILITY;
import static com.arena.engine.cards.NeutralCards.HEALING_POTION;
import static com.arena.engine.cards.NeutralCards.INSIGHT;
import static com.arena.engine.cards.NeutralCards.SHIELDBEARER;
import static com.arena.engine.cards.NeutralCards.STRIKE;

/** "Clerc" in DESIGN.md: healing and attrition. */
public final class Cleric {

    static final List<Card> CARDS = List.of(
            card("Smite", 1, ATTACK).effect(new DealDamage(2)).directDamage(2).build(),
            card("Holy Nova", 4, ATTACK).effect(new DamageChampionAndAllEnemyMinions(2).andThen(new Heal(3)))
                    .directDamage(2).heal(3).build(),
            card("Power Word: Shield", 1, DEFENSE).effect(new GainArmor(3, 2).andThen(new DrawCards(1)))
                    .immediate().armor(3).build(),
            card("Divine Blessing", 2, RESOURCE).effect(new GainMaxMana(1).andThen(new Heal(2)))
                    .immediate().heal(2).build(),
            card("Greater Heal", 3, UTILITY).effect(new Heal(7)).heal(7).build(),
            card("Spirit Healer", 3, UTILITY).effect(new Summon(
                    new MinionTemplate("Spirit Healer", 0, 3, false, new MinionAbility.HealOwnerAtEndOfTurn(2)), 1))
                    .heal(6).build());

    private Cleric() {
    }

    /** The Cleric class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Cleric", new HeroPower("Lesser Heal", 2, new Heal(2)), CARDS,
                Decks.preset(CARDS, List.of(SHIELDBEARER, HEALING_POTION, INSIGHT, STRIKE)));
    }
}
