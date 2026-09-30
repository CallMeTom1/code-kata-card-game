package com.arena.engine.classes;

import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.effects.DealDamage;
import com.arena.engine.effects.DrawCards;
import com.arena.engine.effects.Freeze;
import com.arena.engine.effects.GainArmor;
import com.arena.engine.effects.Summon;

import java.util.List;

import static com.arena.engine.cards.CardBuilder.card;
import static com.arena.engine.cards.CardCategory.ATTACK;
import static com.arena.engine.cards.CardCategory.DEFENSE;
import static com.arena.engine.cards.CardCategory.UTILITY;
import static com.arena.engine.cards.NeutralCards.INSIGHT;
import static com.arena.engine.cards.NeutralCards.MANA_CRYSTAL;
import static com.arena.engine.cards.NeutralCards.QUICK_JAB;
import static com.arena.engine.cards.NeutralCards.WOODEN_SHIELD;

/** Burst spells and necromancy. */
public final class Mage {

    static final List<Card> CARDS = List.of(
            card("Frostbolt", 2, ATTACK).effect(new DealDamage(3).andThen(new Freeze())).directDamage(3).build(),
            card("Fireball", 4, ATTACK).effect(new DealDamage(6)).directDamage(6).build(),
            card("Pyroblast", 8, ATTACK).effect(new DealDamage(10)).directDamage(10).build(),
            card("Raise Skeletons", 3, ATTACK)
                    .effect(new Summon(new MinionTemplate("Skeleton", 1, 1, false), 2)).indirectDamage(4).build(),
            card("Ice Barrier", 3, DEFENSE).effect(new GainArmor(8, 2)).armor(8).build(),
            card("Arcane Intellect", 3, UTILITY).effect(new DrawCards(2)).immediate().build());

    private Mage() {
    }

    /** The Mage class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Mage", new HeroPower("Fireblast", 2, new DealDamage(1)), CARDS,
                Decks.preset(CARDS, List.of(MANA_CRYSTAL, INSIGHT, QUICK_JAB, WOODEN_SHIELD)));
    }
}
