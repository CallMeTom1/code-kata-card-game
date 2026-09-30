package com.arena.engine.cards;

/** The 10 neutral cards from DESIGN.md, available to build every class deck's filler. */
public final class NeutralCards {

    private NeutralCards() {
    }

    public static Card quickJab() {
        return Card.queued("Quick Jab", 1, CardCategory.ATTACK, ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 2));
    }

    public static Card strike() {
        return Card.queued("Strike", 2, CardCategory.ATTACK, ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 4));
    }

    public static Card crushingBlow() {
        return Card.queued("Crushing Blow", 5, CardCategory.ATTACK, ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 8));
    }

    public static Card woodenShield() {
        return Card.queued("Wooden Shield", 1, CardCategory.DEFENSE, ctx -> ctx.caster().addArmor(3, 2));
    }

    public static Card ironWall() {
        return Card.queued("Iron Wall", 3, CardCategory.DEFENSE, ctx -> ctx.caster().addArmor(7, 2));
    }

    public static Card manaCrystal() {
        return Card.immediate("Mana Crystal", 1, CardCategory.RESOURCE, ctx -> ctx.caster().increaseMaxMana(1));
    }

    public static Card insight() {
        return Card.immediate("Insight", 1, CardCategory.UTILITY, ctx -> drawOne(ctx.caster()));
    }

    public static Card healingPotion() {
        return Card.queued("Healing Potion", 2, CardCategory.UTILITY, ctx -> ctx.caster().heal(5));
    }

    private static void drawOne(com.arena.engine.player.Champion champion) {
        if (!champion.deck().isEmpty()) {
            champion.addToHand(champion.deck().pollFirst());
        }
    }
}
