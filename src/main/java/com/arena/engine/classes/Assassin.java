package com.arena.engine.classes;

import com.arena.engine.board.MinionAbility;
import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.effects.ApplyPoison;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.DealDamageIgnoringArmor;
import com.arena.engine.effects.GainTempMana;
import com.arena.engine.effects.GrantEvasion;
import com.arena.engine.effects.Summon;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.ATTACK;
import static com.arena.engine.cards.CardCategory.DEFENSE;
import static com.arena.engine.cards.CardCategory.RESOURCE;
import static com.arena.engine.cards.NeutralCards.HEALING_POTION;
import static com.arena.engine.cards.NeutralCards.MANA_CRYSTAL;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.WILD_WOLF;

/** Cheap cards, combos and poison. */
public final class Assassin {

    static final List<Card> CARDS = List.of(
            card("Backstab", 0, ATTACK).effect(new DealDamage(2)).directDamage(2).text("Deal 2 damage.").build(),
            card("Eviscerate", 3, ATTACK).effect(new DealDamageIgnoringArmor(3, 5)).directDamage(4).text("Deal 3 damage through Armor. Combo: 5 instead.").build(),
            card("Deadly Poison", 2, ATTACK).effect(new ApplyPoison(2, 3)).indirectDamage(6).text("Poison 2 for 3 turns (ignores Armor).").build(),
            card("Venom Spider", 2, ATTACK).effect(new Summon(
                    new MinionTemplate("Venom Spider", 1, 2, false, new MinionAbility.PoisonOnHit(1, 2)), 1))
                    .indirectDamage(4).text("Summon a 1/2 Spider whose hits also poison.").build(),
            card("Evasion", 2, DEFENSE).effect(new GrantEvasion(2)).armor(4).text("Secret: prevent the next damage an enemy deals to you.").build(),
            card("Preparation", 0, RESOURCE).effect(new GainTempMana(2)).immediate().text("Gain 2 mana this turn only.").build());

    private Assassin() {
    }

    /** The Assassin class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Assassin", new HeroPower("Poisoned Dagger", 2, new ApplyPoison(1, 2), "Poison 1 (2 turns)"), CARDS,
                Decks.preset(CARDS, List.of(QUICK_JAB, WILD_WOLF, MANA_CRYSTAL, HEALING_POTION)));
    }
}
