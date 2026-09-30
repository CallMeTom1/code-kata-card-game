package com.pokemonarena.game;

/**
 * A temporary effect attached to a Hero, with an explicit source, value and expiration rule
 * (no anonymous boolean flags).
 * <p>
 * The target is always the owning Hero in this version: damage reductions protect the Hero and
 * attack buffs boost the damage of its Attack effects. Effects created by a Pokémon belong to
 * the Hero, so they survive that Pokémon's death.
 *
 * @param kind             what the effect does
 * @param source           id of the card or Hero Power that created it
 * @param value            amount of damage prevented or added
 * @param expiresAfterTurn absolute turn number after the END phase of which the effect is lost,
 *                         or {@link #NEVER_EXPIRES} when it lasts until it is consumed
 */
public record TemporaryEffect(Kind kind, String source, int value, int expiresAfterTurn) {

    public static final int NEVER_EXPIRES = Integer.MAX_VALUE;

    public enum Kind {
        /** Reduces the next damage received by the Hero. */
        DAMAGE_REDUCTION,
        /** Adds damage to the next damage event coming from an Attack effect. */
        ATTACK_BUFF
    }

    public TemporaryEffect {
        if (value <= 0) {
            throw new IllegalArgumentException("a temporary effect must have a positive value: " + value);
        }
    }

    public static TemporaryEffect damageReduction(String source, int value, int expiresAfterTurn) {
        return new TemporaryEffect(Kind.DAMAGE_REDUCTION, source, value, expiresAfterTurn);
    }

    public static TemporaryEffect attackBuff(String source, int value, int expiresAfterTurn) {
        return new TemporaryEffect(Kind.ATTACK_BUFF, source, value, expiresAfterTurn);
    }

    public boolean isExpiredAfter(int turnNumber) {
        return turnNumber >= expiresAfterTurn;
    }
}
