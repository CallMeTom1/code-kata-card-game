package com.arena.engine.classes;

import com.arena.engine.board.MinionTemplate;
import com.arena.engine.cards.Card;
import com.arena.engine.effects.DamageEqualToArmor;
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
import static com.arena.engine.cards.NeutralCards.CRUSHING_BLOW;
import static com.arena.engine.cards.NeutralCards.HEALING_POTION;
import static com.arena.engine.cards.NeutralCards.IRON_WALL;
import static com.arena.engine.cards.NeutralCards.STRIKE;

/** Armor and endurance. */
public final class Tank {

    static final List<Card> CARDS = List.of(
            card("Shield Slam", 1, ATTACK).effect(new DamageEqualToArmor()).directDamage(3).build(),
            card("Shield Block", 3, DEFENSE).effect(new GainArmor(5, 2).andThen(new DrawCards(1)))
                    .immediate().armor(5).build(),
            card("Fortress", 5, DEFENSE).effect(new GainArmor(12, 3)).armor(12).build(),
            card("Iron Golem", 4, DEFENSE)
                    .effect(new Summon(new MinionTemplate("Iron Golem", 2, 6, true), 1)).armor(6).taunt().build(),
            card("War Chest", 2, RESOURCE).effect(new GainMaxMana(1).andThen(new GainArmor(2, 2)))
                    .immediate().armor(2).build(),
            card("Last Stand", 4, UTILITY).effect(new Heal(8)).heal(8).build());

    private Tank() {
    }

    /** The Tank class with its preset deck from DESIGN.md. */
    public static HeroClass definition() {
        return new HeroClass("Tank", new HeroPower("Armor Up", 2, new GainArmor(2, 3), "Armor 2 (3 turns)"), CARDS,
                Decks.preset(CARDS, List.of(IRON_WALL, STRIKE, CRUSHING_BLOW, HEALING_POTION)));
    }
}
