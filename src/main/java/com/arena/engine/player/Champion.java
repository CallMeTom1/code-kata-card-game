package com.arena.engine.player;

import com.arena.engine.cards.Card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Mutable state of one side of the duel; only the engine and effects change it. */
public final class Champion {

    /** Starting and maximum HP, from the brief. */
    public static final int MAX_HP = 30;
    /** Mana cap, as in Hearthstone. */
    public static final int MAX_MANA = 10;

    private final String name;
    private final Deque<Card> deck;
    private final List<Card> hand = new ArrayList<>();
    private int hp = MAX_HP;
    private int maxMana;
    private int mana;

    /** Keeps the deck order as given, because shuffling belongs to the seeded match setup. */
    public Champion(String name, List<Card> deck) {
        this.name = name;
        this.deck = new ArrayDeque<>(deck);
    }

    /** Name used in every log line and event. */
    public String name() {
        return name;
    }

    /** Current HP, never below 0. */
    public int hp() {
        return hp;
    }

    /** A champion at 0 HP has lost; the engine checks this to end the match. */
    public boolean isDead() {
        return hp == 0;
    }

    /** Removes HP after defenses were applied elsewhere; floors at 0 so logs never show negative HP. */
    public void loseHp(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /** Restores HP without exceeding the starting 30, as in Hearthstone. */
    public void heal(int amount) {
        hp = Math.min(MAX_HP, hp + amount);
    }

    /** Mana available right now, to know what can still be played. */
    public int mana() {
        return mana;
    }

    /** Mana crystals owned, which grow each turn. */
    public int maxMana() {
        return maxMana;
    }

    /** Sets the crystals for the turn and fills them, capped at 10. */
    public void refillMana(int newMaxMana) {
        maxMana = Math.min(MAX_MANA, newMaxMana);
        mana = maxMana;
    }

    /** Pays for a card; refuses instead of going negative so an illegal play cannot slip through. */
    public void spendMana(int amount) {
        if (amount > mana) {
            throw new IllegalStateException(name + " cannot spend " + amount + " mana with " + mana);
        }
        mana -= amount;
    }

    /** Cards in hand, read-only so only the engine changes them. */
    public List<Card> hand() {
        return List.copyOf(hand);
    }

    /** Puts a drawn or given card in hand. */
    public void addToHand(Card card) {
        hand.add(card);
    }

    /** Remaining deck, top card first, read-only. */
    public List<Card> deck() {
        return List.copyOf(deck);
    }
}
