package com.arena.engine.cards;

import com.arena.engine.board.MinionTemplate;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.DrawCards;
import com.arena.engine.effects.GainArmor;
import com.arena.engine.effects.GainMaxMana;
import com.arena.engine.effects.GainTempMana;
import com.arena.engine.effects.Heal;
import com.arena.engine.effects.Summon;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.ATTACK;
import static com.arena.engine.cards.CardCategory.DEFENSE;
import static com.arena.engine.cards.CardCategory.RESOURCE;
import static com.arena.engine.cards.CardCategory.UTILITY;

/** Cards any class can put in its deck (DESIGN.md, "Neutral cards"). */
public final class NeutralCards {

    public static final Card QUICK_JAB = card("Quick Jab", 1, ATTACK).effect(new DealDamage(2)).directDamage(2).text("Deal 2 damage.").build();
    public static final Card STRIKE = card("Strike", 2, ATTACK).effect(new DealDamage(4)).directDamage(4).text("Deal 4 damage.").build();
    public static final Card CRUSHING_BLOW = card("Crushing Blow", 5, ATTACK)
            .effect(new DealDamage(8)).directDamage(8).text("Deal 8 damage.").build();
    public static final Card WILD_WOLF = card("Wild Wolf", 2, ATTACK)
            .effect(new Summon(new MinionTemplate("Wolf", 2, 2, false), 1)).indirectDamage(4).text("Summon a 2/2 Wolf.").build();
    public static final Card WOODEN_SHIELD = card("Wooden Shield", 1, DEFENSE)
            .effect(new GainArmor(2, 2)).armor(2).text("Gain 2 Armor for 2 turns.").build();
    public static final Card IRON_WALL = card("Iron Wall", 3, DEFENSE).effect(new GainArmor(5, 2)).armor(5).text("Gain 5 Armor for 2 turns.").build();
    public static final Card SHIELDBEARER = card("Shieldbearer", 1, DEFENSE)
            .effect(new Summon(new MinionTemplate("Shieldbearer", 0, 3, true), 1)).armor(3).taunt().text("Summon a 0/3 Shieldbearer with Taunt.").build();
    public static final Card MANA_CRYSTAL = card("Mana Crystal", 1, RESOURCE)
            .effect(new GainMaxMana(1)).immediate().text("Gain an empty mana crystal.").build();
    public static final Card INSIGHT = card("Insight", 1, UTILITY).effect(new DrawCards(1)).immediate().text("Draw a card.").build();
    public static final Card HEALING_POTION = card("Healing Potion", 2, UTILITY).effect(new Heal(4)).heal(4).text("Restore 4 HP.").build();

    /** Given to the second player, never part of a deck. */
    public static final Card THE_COIN = card("The Coin", 0, RESOURCE).effect(new GainTempMana(1)).immediate().text("Gain 1 mana this turn only.").build();

    private NeutralCards() {
    }

    /** The 10 neutral cards a deck may contain (The Coin excluded). */
    public static List<Card> all() {
        return List.of(QUICK_JAB, STRIKE, CRUSHING_BLOW, WILD_WOLF, WOODEN_SHIELD, IRON_WALL, SHIELDBEARER,
                MANA_CRYSTAL, INSIGHT, HEALING_POTION);
    }
}
