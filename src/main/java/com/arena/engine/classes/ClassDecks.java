package com.arena.engine.classes;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.MageCards;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.cards.TankCards;

import java.util.ArrayList;
import java.util.List;

/** Builds each 20-card class deck (class cards x2 + neutral filler), exactly as listed in DESIGN.md. */
public final class ClassDecks {

    private ClassDecks() {
    }

    /**
     * Mage's spell-only MVP deck (Raise Skeletons is left out until minions exist):
     * Frostbolt, Fireball, Pyroblast, Ice Barrier, Arcane Intellect x2 each, plus
     * Mana Crystal, Insight, Quick Jab, Wooden Shield, Healing Potion x2 each as filler.
     */
    public static HeroClass mage() {
        List<Card> deck = new ArrayList<>();
        addTwice(deck, MageCards::frostbolt);
        addTwice(deck, MageCards::fireball);
        addTwice(deck, MageCards::pyroblast);
        addTwice(deck, MageCards::iceBarrier);
        addTwice(deck, MageCards::arcaneIntellect);
        addTwice(deck, NeutralCards::manaCrystal);
        addTwice(deck, NeutralCards::insight);
        addTwice(deck, NeutralCards::quickJab);
        addTwice(deck, NeutralCards::woodenShield);
        addTwice(deck, NeutralCards::healingPotion);
        return new HeroClass("Mage", MageCards.fireblast(), deck);
    }

    /**
     * Tank's spell-only MVP deck (Iron Golem is left out until minions exist):
     * Shield Slam, Shield Block, Fortress, War Chest, Last Stand x2 each, plus
     * Iron Wall, Strike, Crushing Blow, Healing Potion, Quick Jab x2 each as filler.
     */
    public static HeroClass tank() {
        List<Card> deck = new ArrayList<>();
        addTwice(deck, TankCards::shieldSlam);
        addTwice(deck, TankCards::shieldBlock);
        addTwice(deck, TankCards::fortress);
        addTwice(deck, TankCards::warChest);
        addTwice(deck, TankCards::lastStand);
        addTwice(deck, NeutralCards::ironWall);
        addTwice(deck, NeutralCards::strike);
        addTwice(deck, NeutralCards::crushingBlow);
        addTwice(deck, NeutralCards::healingPotion);
        addTwice(deck, NeutralCards::quickJab);
        return new HeroClass("Tank", TankCards.armorUp(), deck);
    }

    private static void addTwice(List<Card> deck, java.util.function.Supplier<Card> factory) {
        deck.add(factory.get());
        deck.add(factory.get());
    }
}
