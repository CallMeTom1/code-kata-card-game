package com.arena.engine.cards;

import com.arena.engine.effects.Effect;

/** Reads like the card tables of DESIGN.md, so card lists stay easy to compare with the design. */
public final class CardBuilder {

    private final String name;
    private final int cost;
    private final CardCategory category;
    private Effect effect = Effect.NONE;
    private boolean immediate;
    private int damage;
    private int heal;
    private int armor;
    private boolean taunt;
    private boolean attackBuff;
    private boolean usesAttackBonus;

    private CardBuilder(String name, int cost, CardCategory category) {
        this.name = name;
        this.cost = cost;
        this.category = category;
    }

    /** Starts a card definition. */
    public static CardBuilder card(String name, int cost, CardCategory category) {
        return new CardBuilder(name, cost, category);
    }

    /** What the card does. */
    public CardBuilder effect(Effect effect) {
        this.effect = effect;
        return this;
    }

    /** Applies during the play phase (Resource and draw cards). */
    public CardBuilder immediate() {
        this.immediate = true;
        return this;
    }

    /** Direct damage that also benefits from "next Attack +X". */
    public CardBuilder directDamage(int amount) {
        this.damage = amount;
        this.usesAttackBonus = true;
        return this;
    }

    /** Expected damage that does not use attack buffs (minions, poison). */
    public CardBuilder indirectDamage(int amount) {
        this.damage = amount;
        return this;
    }

    public CardBuilder heal(int amount) {
        this.heal = amount;
        return this;
    }

    public CardBuilder armor(int amount) {
        this.armor = amount;
        return this;
    }

    public CardBuilder taunt() {
        this.taunt = true;
        return this;
    }

    public CardBuilder attackBuff() {
        this.attackBuff = true;
        return this;
    }

    public Card build() {
        return new Card(name, cost, category, effect, immediate,
                new CardTraits(damage, heal, armor, taunt, attackBuff, usesAttackBonus));
    }
}
