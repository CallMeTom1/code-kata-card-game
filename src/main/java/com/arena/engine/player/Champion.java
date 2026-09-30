package com.arena.engine.player;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Mutable match-time state of one champion. Kept as a plain data holder on purpose:
 * the rules that mutate it (damage, phases, tie-break) live in dedicated engine classes (SRP).
 */
public final class Champion {

    public static final int STARTING_HP = 30;
    public static final int MAX_HP = 30;
    public static final int HAND_LIMIT = 10;
    public static final int MAX_MANA_CAP = 10;

    private final String name;
    private final HeroClass heroClass;
    private int hp = STARTING_HP;
    private int maxMana;
    private int mana;
    private int manaPenaltyNextTurn;
    private int fatigue;
    private boolean heroPowerUsedThisTurn;
    private boolean cardPlayedThisTurn;
    private int nextAttackBonus;
    private int evasionCharges;

    private final List<Card> hand = new ArrayList<>();
    private final Deque<Card> deck = new ArrayDeque<>();
    private final List<Armor> armors = new ArrayList<>();
    private int parryAmount;
    private int parryTurnsLeft;
    private final List<Poison> poisons = new ArrayList<>();

    public Champion(String name, HeroClass heroClass) {
        this.name = name;
        this.heroClass = heroClass;
    }

    public String name() {
        return name;
    }

    public HeroClass heroClass() {
        return heroClass;
    }

    public int hp() {
        return hp;
    }

    /** Removes HP directly (armor/Parry are applied earlier, by the DamageResolver). */
    public void loseHp(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /** Restores HP, never above the 30 HP cap. */
    public void heal(int amount) {
        hp = Math.min(MAX_HP, hp + amount);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int maxMana() {
        return maxMana;
    }

    public int mana() {
        return mana;
    }

    /** Grows max mana by 1 (capped at 10) and refills current mana, minus any Freeze penalty. */
    public void growAndRefillMana() {
        maxMana = Math.min(MAX_MANA_CAP, maxMana + 1);
        mana = Math.max(0, maxMana - manaPenaltyNextTurn);
        manaPenaltyNextTurn = 0;
    }

    /** Grants extra mana this turn only (Preparation, The Coin); never above the mana cap. */
    public void addTemporaryMana(int amount) {
        mana = Math.min(MAX_MANA_CAP, mana + amount);
    }

    /** Permanently raises max mana (Mana Crystal-like effects), capped at 10. */
    public void increaseMaxMana(int amount) {
        maxMana = Math.min(MAX_MANA_CAP, maxMana + amount);
    }

    public boolean canAfford(int cost) {
        return mana >= cost;
    }

    /** Spends mana for a card or hero power; callers must check {@link #canAfford(int)} first. */
    public void spendMana(int cost) {
        mana = Math.max(0, mana - cost);
    }

    /** Freeze: reduces mana on the champion's next mana phase instead of the current one. */
    public void freezeNextTurn() {
        manaPenaltyNextTurn += 1;
    }

    public List<Card> hand() {
        return hand;
    }

    public Deque<Card> deck() {
        return deck;
    }

    /** Adds a card to hand, or burns it if the hand is already full (returns true if burned). */
    public boolean addToHand(Card card) {
        if (hand.size() >= HAND_LIMIT) {
            return true;
        }
        hand.add(card);
        return false;
    }

    public void removeFromHand(Card card) {
        hand.remove(card);
    }

    /** Fatigue damage grows by 1 each time the deck is empty on this champion's draw. */
    public int nextFatigueDamage() {
        fatigue += 1;
        return fatigue;
    }

    public boolean heroPowerUsedThisTurn() {
        return heroPowerUsedThisTurn;
    }

    public void markHeroPowerUsed() {
        heroPowerUsedThisTurn = true;
    }

    public boolean cardPlayedThisTurn() {
        return cardPlayedThisTurn;
    }

    public void markCardPlayed() {
        cardPlayedThisTurn = true;
    }

    /** Resets once-per-turn flags at the start of this champion's own turn. */
    public void resetTurnFlags() {
        heroPowerUsedThisTurn = false;
        cardPlayedThisTurn = false;
    }

    /** Stores a bonus consumed by the next Attack card this champion plays (Sharpen, Battle Cry). */
    public void addNextAttackBonus(int amount) {
        nextAttackBonus += amount;
    }

    /** Returns and clears the pending next-attack bonus, so it is spent exactly once. */
    public int consumeNextAttackBonus() {
        int bonus = nextAttackBonus;
        nextAttackBonus = 0;
        return bonus;
    }

    public void addEvasion() {
        evasionCharges += 1;
    }

    public boolean hasEvasion() {
        return evasionCharges > 0;
    }

    /** Cancels the next incoming Attack card's damage; returns true if evasion was consumed. */
    public boolean consumeEvasion() {
        if (evasionCharges <= 0) {
            return false;
        }
        evasionCharges -= 1;
        return true;
    }

    public List<Armor> armors() {
        return armors;
    }

    public void addArmor(int amount, int turns) {
        armors.add(Armor.of(amount, turns));
    }

    public int totalArmor() {
        return armors.stream().mapToInt(Armor::amount).sum();
    }

    /** Absorbs as much of {@code amount} as available armor allows, removing spent armor charges. */
    public int absorbWithArmor(int amount) {
        int remaining = amount;
        var iterator = armors.listIterator();
        while (iterator.hasNext() && remaining > 0) {
            Armor armor = iterator.next();
            if (armor.amount() <= remaining) {
                remaining -= armor.amount();
                iterator.remove();
            } else {
                iterator.set(new Armor(armor.amount() - remaining, armor.turnsLeft()));
                remaining = 0;
            }
        }
        return amount - remaining;
    }

    /** Ages every armor charge by one owner turn and drops the ones that expire. */
    public List<Armor> tickArmorForNewTurn() {
        List<Armor> expired = new ArrayList<>();
        List<Armor> next = new ArrayList<>();
        for (Armor armor : armors) {
            Armor ticked = armor.ticked();
            if (ticked.expired()) {
                expired.add(armor);
            } else {
                next.add(ticked);
            }
        }
        armors.clear();
        armors.addAll(next);
        return expired;
    }

    public void addParry(int amount, int turns) {
        parryAmount = amount;
        parryTurnsLeft = turns;
    }

    public int parryAmount() {
        return parryTurnsLeft > 0 ? parryAmount : 0;
    }

    public void tickParryForNewTurn() {
        if (parryTurnsLeft > 0) {
            parryTurnsLeft -= 1;
        }
    }

    public List<Poison> poisons() {
        return poisons;
    }

    public void addPoison(int amount, int turns) {
        poisons.add(new Poison(amount, turns));
    }

    /** Applies all poison ticks for this champion's new turn and ages/removes expired stacks. */
    public int tickPoisonForNewTurn() {
        int total = 0;
        List<Poison> next = new ArrayList<>();
        for (Poison poison : poisons) {
            total += poison.amount();
            Poison ticked = poison.ticked();
            if (!ticked.expired()) {
                next.add(ticked);
            }
        }
        poisons.clear();
        poisons.addAll(next);
        return total;
    }
}
