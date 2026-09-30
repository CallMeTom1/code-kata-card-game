package com.arena.engine.cards;

/**
 * Numbers bots read to rank cards, so strategies never have to inspect effects.
 *
 * @param damage          expected damage to the enemy champion (a minion counts twice its attack)
 * @param heal            HP the caster gets back
 * @param armor           protection value (armor, Parry, Evasion)
 * @param taunt           summons a Taunt minion
 * @param attackBuff      buffs the next Attack card
 * @param usesAttackBonus a damaging Attack card that consumes "next Attack +X" buffs
 */
public record CardTraits(int damage, int heal, int armor, boolean taunt, boolean attackBuff,
                         boolean usesAttackBonus) {

    /** Traits of a card with no combat value (draw, mana). */
    public static final CardTraits NONE = new CardTraits(0, 0, 0, false, false, false);
}
