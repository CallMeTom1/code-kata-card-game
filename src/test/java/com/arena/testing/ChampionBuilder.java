package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.player.Champion;

import java.util.ArrayList;
import java.util.List;

/** Builds champions in a given state so each test only states what matters to it. */
public final class ChampionBuilder {

    private String name = "P1";
    private int hp = Champion.MAX_HP;
    private int mana = 0;
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> hand = new ArrayList<>();

    private ChampionBuilder() {
    }

    /** Entry point that reads well in tests: {@code aChampion().withHp(10).build()}. */
    public static ChampionBuilder aChampion() {
        return new ChampionBuilder();
    }

    public ChampionBuilder named(String name) {
        this.name = name;
        return this;
    }

    public ChampionBuilder withHp(int hp) {
        this.hp = hp;
        return this;
    }

    public ChampionBuilder withMana(int mana) {
        this.mana = mana;
        return this;
    }

    public ChampionBuilder withDeck(Card... cards) {
        deck.addAll(List.of(cards));
        return this;
    }

    public ChampionBuilder withHand(Card... cards) {
        hand.addAll(List.of(cards));
        return this;
    }

    public Champion build() {
        Champion champion = new Champion(name, deck);
        champion.loseHp(Champion.MAX_HP - hp);
        champion.refillMana(mana);
        hand.forEach(champion::addToHand);
        return champion;
    }
}
