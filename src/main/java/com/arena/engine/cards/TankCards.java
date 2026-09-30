package com.arena.engine.cards;

/** Tank's class cards: armor and endurance, per DESIGN.md. */
public final class TankCards {

    private TankCards() {
    }

    public static Card shieldSlam() {
        return Card.queued("Shield Slam", 1, CardCategory.ATTACK,
                ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), ctx.caster().totalArmor()));
    }

    public static Card shieldBlock() {
        return Card.queued("Shield Block", 3, CardCategory.DEFENSE, ctx -> {
            ctx.caster().addArmor(5, 2);
            var deck = ctx.caster().deck();
            if (!deck.isEmpty()) {
                ctx.caster().addToHand(deck.pollFirst());
            }
        });
    }

    public static Card fortress() {
        return Card.queued("Fortress", 5, CardCategory.DEFENSE, ctx -> ctx.caster().addArmor(12, 3));
    }

    public static Card warChest() {
        return Card.immediate("War Chest", 2, CardCategory.RESOURCE, ctx -> {
            ctx.caster().increaseMaxMana(1);
            ctx.caster().addArmor(2, 2);
        });
    }

    public static Card lastStand() {
        return Card.queued("Last Stand", 4, CardCategory.UTILITY, ctx -> ctx.caster().heal(8));
    }

    public static com.arena.engine.classes.HeroPower armorUp() {
        return new com.arena.engine.classes.HeroPower("Armor Up", 2, ctx -> ctx.caster().addArmor(2, 3));
    }
}
