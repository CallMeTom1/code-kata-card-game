package com.pokemonarena.cards.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectTest {

    private final RecordingEffectContext context = new RecordingEffectContext();

    @Test
    void damageHeroEffectDamagesTheOpposingHeroAsAnAttackEffect() {
        new DamageHeroEffect(4).apply(context);

        assertEquals(java.util.List.of("damageOpposingHero(4,true)"), context.calls());
    }

    @Test
    void damagePokemonEffectHitsTheTargetedPokemon() {
        new DamagePokemonEffect(3).apply(context);

        assertEquals(java.util.List.of("damageTargetPokemon(3,true)"), context.calls());
    }

    @Test
    void thresholdDamageUsesTheBaseAmountOnAHealthyHero() {
        context.opposingHeroHp(11);

        new ThresholdDamageHeroEffect(3, 10, 4).apply(context);

        assertEquals(java.util.List.of("damageOpposingHero(3,true)"), context.calls());
    }

    @Test
    void thresholdDamageUsesTheBoostedAmountAtOrBelowTheThreshold() {
        context.opposingHeroHp(10);

        new ThresholdDamageHeroEffect(3, 10, 4).apply(context);

        assertEquals(java.util.List.of("damageOpposingHero(4,true)"), context.calls());
    }

    @Test
    void damageReductionIsQueuedWithoutTimedExpiryByDefault() {
        new DamageReductionEffect(4).apply(context);

        assertEquals(java.util.List.of("addHeroDamageReduction(4,false)"), context.calls());
    }

    @Test
    void frozenBarrierStyleReductionExpiresAtTheEndOfTheFollowingTurn() {
        new DamageReductionEffect(4, true).apply(context);

        assertEquals(java.util.List.of("addHeroDamageReduction(4,true)"), context.calls());
    }

    @Test
    void attackBuffCanBeLimitedToTheCurrentTurn() {
        new AttackBuffEffect(1, true).apply(context);
        new AttackBuffEffect(3).apply(context);

        assertEquals(java.util.List.of("addAttackBuff(1,true)", "addAttackBuff(3,false)"), context.calls());
    }

    @Test
    void compositeEffectAppliesItsPartsInOrder() {
        new CompositeEffect(new HealHeroEffect(3), new AttackBuffEffect(2)).apply(context);

        assertEquals(java.util.List.of("healSelfHero(3)", "addAttackBuff(2,false)"), context.calls());
    }

    @Test
    void resourceEffectsDelegateToTheEngine() {
        new RestoreManaEffect(2).apply(context);
        new IncreaseMaxManaEffect(1, 1).apply(context);
        new DrawEffect(2).apply(context);
        new ReturnPokemonFromDiscardEffect().apply(context);

        assertEquals(java.util.List.of(
                        "restoreAvailableMana(2)",
                        "increaseMaxMana(1,1)",
                        "drawCards(2)",
                        "returnChosenPokemonFromDiscard()"),
                context.calls());
    }

    @Test
    void everyEffectDescribesItself() {
        assertTrue(new DrawEffect(1).describe().contains("1 card"));
        assertTrue(new CompositeEffect(new DamageHeroEffect(2), new DrawEffect(1))
                .describe().contains("then"));
    }
}
