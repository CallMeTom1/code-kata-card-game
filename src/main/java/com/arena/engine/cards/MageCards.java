package com.arena.engine.cards;

/** Mage's class cards: burst spells and necromancy, per DESIGN.md. */
public final class MageCards {

    private MageCards() {
    }

    public static Card frostbolt() {
        return Card.queued("Frostbolt", 2, CardCategory.ATTACK, ctx -> {
            ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 3);
            ctx.opponent().freezeNextTurn();
        });
    }

    public static Card fireball() {
        return Card.queued("Fireball", 4, CardCategory.ATTACK, ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 6));
    }

    public static Card pyroblast() {
        return Card.queued("Pyroblast", 8, CardCategory.ATTACK, ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 10));
    }

    public static Card iceBarrier() {
        return Card.queued("Ice Barrier", 3, CardCategory.DEFENSE, ctx -> ctx.caster().addArmor(8, 2));
    }

    public static Card arcaneIntellect() {
        return Card.immediate("Arcane Intellect", 3, CardCategory.UTILITY, ctx -> {
            drawOne(ctx);
            drawOne(ctx);
        });
    }

    public static com.arena.engine.classes.HeroPower fireblast() {
        return new com.arena.engine.classes.HeroPower("Fireblast", 2,
                ctx -> ctx.damage().deal(ctx.caster().name(), ctx.opponent(), 1));
    }

    private static void drawOne(com.arena.engine.effects.EffectContext ctx) {
        var deck = ctx.caster().deck();
        if (!deck.isEmpty()) {
            ctx.caster().addToHand(deck.pollFirst());
        }
    }
}
