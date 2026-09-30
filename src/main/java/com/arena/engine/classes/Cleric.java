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
            card("Smite", 1, ATTACK).effect(new DealDamage(2)).directDamage(2).text("Deal 2 damage.").build(),
            card("Holy Nova", 5, ATTACK).effect(new DamageChampionAndAllEnemyMinions(2).andThen(new Heal(2)))
                    .directDamage(2).heal(2).text("Deal 2 damage to the enemy champion and each enemy minion. Restore 2 HP.").build(),
            card("Power Word: Shield", 1, DEFENSE).effect(new GainArmor(3, 2).andThen(new DrawCards(1)))
                    .immediate().armor(3).text("Gain 3 Armor for 2 turns. Draw a card.").build(),
            card("Divine Blessing", 2, RESOURCE).effect(new GainMaxMana(1).andThen(new Heal(2)))
                    .immediate().heal(2).text("Gain an empty mana crystal. Restore 2 HP.").build(),
            card("Greater Heal", 3, UTILITY).effect(new Heal(6)).heal(6).text("Restore 6 HP.").build(),
            card("Spirit Healer", 3, UTILITY).effect(new Summon(
                    new MinionTemplate("Spirit Healer", 0, 3, true, new MinionAbility.HealOwnerAtEndOfTurn(2)), 1))
                    .heal(6).taunt().text("Summon a 0/3 Spirit with Taunt that restores 2 HP to you at the end of your turns.").build());

    private Cleric() {
    }

    /** The Cleric class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Cleric", new HeroPower("Lesser Heal", 2, new Heal(2), "heal 2 HP"), CARDS,
                Decks.preset(CARDS, List.of(SHIELDBEARER, HEALING_POTION, INSIGHT, STRIKE)));
    }
}
