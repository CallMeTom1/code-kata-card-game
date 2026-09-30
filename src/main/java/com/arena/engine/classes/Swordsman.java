package com.arena.engine.classes;

import com.arena.engine.cards.Card;
import com.arena.engine.effects.BuffNextAttack;
import com.arena.engine.effects.DamageChampionAndAllEnemyMinions;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.GainMaxMana;
import com.arena.engine.effects.GainParry;
import com.arena.engine.effects.MultiHit;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.ATTACK;
import static com.arena.engine.cards.CardCategory.DEFENSE;
import static com.arena.engine.cards.CardCategory.RESOURCE;
import static com.arena.engine.cards.CardCategory.UTILITY;
import static com.arena.engine.cards.NeutralCards.MANA_CRYSTAL;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.STRIKE;
import static com.arena.engine.cards.NeutralCards.WILD_WOLF;
import static com.arena.engine.cards.NeutralCards.WOODEN_SHIELD;

/** "Épéiste" in DESIGN.md: multi-hit, buffs and minion clearing, no summons. */
public final class Swordsman {

    static final List<Card> CARDS = List.of(
            card("Twin Blades", 3, ATTACK).effect(new MultiHit(2, 3)).directDamage(6).text("Hit twice for 3 damage.").build(),
            card("Whirlwind Slash", 5, ATTACK).effect(new DamageChampionAndAllEnemyMinions(3)).directDamage(3).text("Deal 3 damage to the enemy champion and each enemy minion.").build(),
            card("Riposte", 2, DEFENSE).effect(new GainParry(2, 1).andThen(new DealDamage(2))).armor(2).text("Parry 2 for 1 turn. Deal 2 damage.").build(),
            card("Focus Training", 1, RESOURCE).effect(new GainMaxMana(1).andThen(new BuffNextAttack(1)))
                    .immediate().attackBuff().text("Gain an empty mana crystal. Your next Attack card deals +1.").build(),
            card("Battle Cry", 1, UTILITY).effect(new BuffNextAttack(3)).attackBuff().text("Your next Attack card deals +3 damage per hit.").build());

    private Swordsman() {
    }

    /** The Swordsman class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Swordsman", new HeroPower("Sharpen", 2, new BuffNextAttack(2), "next Attack card +2 damage"), CARDS,
                Decks.preset(CARDS, List.of(QUICK_JAB, STRIKE, WILD_WOLF, MANA_CRYSTAL, WOODEN_SHIELD)));
    }
}
