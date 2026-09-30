package com.pokemonarena.cards.effects;

import java.util.ArrayList;
import java.util.List;

/**
 * Test double that records what an effect asked the engine to do, so effects can be tested
 * before the engine exists.
 */
final class RecordingEffectContext implements EffectContext {

    private final List<String> calls = new ArrayList<>();
    private int opposingHeroHp = 30;

    List<String> calls() {
        return calls;
    }

    void opposingHeroHp(int hp) {
        this.opposingHeroHp = hp;
    }

    @Override
    public int opposingHeroHp() {
        return opposingHeroHp;
    }

    @Override
    public void damageOpposingHero(int amount, boolean fromAttackEffect) {
        calls.add("damageOpposingHero(" + amount + "," + fromAttackEffect + ")");
    }

    @Override
    public void damageTargetPokemon(int amount, boolean fromAttackEffect) {
        calls.add("damageTargetPokemon(" + amount + "," + fromAttackEffect + ")");
    }

    @Override
    public void healSelfHero(int amount) {
        calls.add("healSelfHero(" + amount + ")");
    }

    @Override
    public void drawCards(int count) {
        calls.add("drawCards(" + count + ")");
    }

    @Override
    public void restoreAvailableMana(int amount) {
        calls.add("restoreAvailableMana(" + amount + ")");
    }

    @Override
    public void increaseMaxMana(int maxManaGain, int availableManaGain) {
        calls.add("increaseMaxMana(" + maxManaGain + "," + availableManaGain + ")");
    }

    @Override
    public void addHeroDamageReduction(int amount, boolean expiresAtEndOfFollowingTurn) {
        calls.add("addHeroDamageReduction(" + amount + "," + expiresAtEndOfFollowingTurn + ")");
    }

    @Override
    public void addAttackBuff(int amount, boolean thisTurnOnly) {
        calls.add("addAttackBuff(" + amount + "," + thisTurnOnly + ")");
    }

    @Override
    public void returnChosenPokemonFromDiscard() {
        calls.add("returnChosenPokemonFromDiscard()");
    }
}
