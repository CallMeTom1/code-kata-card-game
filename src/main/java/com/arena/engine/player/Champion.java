package com.arena.engine.player;

import com.arena.engine.board.Board;
import com.arena.engine.cards.Card;
import com.arena.engine.combat.Defenses;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Mutable state of one side of the duel; only the engine and effects change it. */
public final class Champion {

    /** Starting and maximum HP, from the brief. */
    public static final int MAX_HP = 30;
    /** Mana cap, as in Hearthstone. */
    public static final int MAX_MANA = 10;

    private final String name;
    private final Deque<Card> deck;
    private final List<Card> hand = new ArrayList<>();
    private final Defenses defenses = new Defenses();
    private final Board board = new Board();
    private final List<Poison> poisons = new ArrayList<>();
    private int hp = MAX_HP;
    private int maxMana;
    private int mana;
    private boolean frozen;
    private int attackBonus;
    private int fatigue;

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

    /** Armor, Parry and Evasion, kept in their own class. */
    public Defenses defenses() {
        return defenses;
    }

    /** This champion's minions. */
    public Board board() {
        return board;
    }

    /** Mana phase: one more crystal (up to 10), refilled, minus 1 if frozen last turn. */
    public void growMana() {
        refillMana(maxMana + 1);
        if (frozen) {
            mana = Math.max(0, mana - 1);
            frozen = false;
        }
    }

    /** Freeze: this champion will get 1 mana less on its next turn. */
    public void freeze() {
        frozen = true;
    }

    /** True until the frozen mana phase has happened. */
    public boolean isFrozen() {
        return frozen;
    }

    /** Temporary mana (The Coin, Preparation); total mana stays capped at 10 as in Hearthstone. */
    public void gainMana(int amount) {
        mana = Math.min(MAX_MANA, mana + amount);
    }

    /** Permanent empty crystals (Mana Crystal): usable from the next turn, capped at 10. */
    public void gainMaxMana(int amount) {
        maxMana = Math.min(MAX_MANA, maxMana + amount);
    }

    /** Stores a "next Attack +X" buff; several buffs add up. */
    public void addAttackBonus(int amount) {
        attackBonus += amount;
    }

    /** Pending bonus, for bots and logs. */
    public int attackBonus() {
        return attackBonus;
    }

    /** Hands the whole bonus to the Attack card that uses it, and clears it. */
    public int takeAttackBonus() {
        int bonus = attackBonus;
        attackBonus = 0;
        return bonus;
    }

    /** Adds a poison; poisons stack. */
    public void applyPoison(int amount, int turns) {
        poisons.add(new Poison(amount, turns));
    }

    /** Total poison damage that will tick at the start of the next turn. */
    public int poison() {
        return poisons.stream().mapToInt(p -> p.amount).sum();
    }

    /** Called at the start of this champion's turn; returns the damage to apply and ages every poison. */
    public int tickPoison() {
        int damage = poison();
        poisons.forEach(p -> p.turnsLeft--);
        poisons.removeIf(p -> p.turnsLeft <= 0);
        return damage;
    }

    /** Fatigue grows by 1 each time the deck is empty, as in Hearthstone. */
    public int nextFatigue() {
        return ++fatigue;
    }

    /** Takes a card out of the hand when it is played. */
    public Card removeFromHand(int index) {
        return hand.remove(index);
    }

    /** Draws the top card, if any; fatigue is handled by the caller. */
    public Optional<Card> takeTopCard() {
        return Optional.ofNullable(deck.pollFirst());
    }

    /** Shuffles cards back into the deck (setup, mulligan) with the match's seeded random. */
    public void shuffleIntoDeck(List<Card> cards, Random random) {
        List<Card> all = new ArrayList<>(deck);
        all.addAll(cards);
        Collections.shuffle(all, random);
        deck.clear();
        deck.addAll(all);
    }

    private static final class Poison {
        private final int amount;
        private int turnsLeft;

        private Poison(int amount, int turnsLeft) {
            this.amount = amount;
            this.turnsLeft = turnsLeft;
        }
    }
}
