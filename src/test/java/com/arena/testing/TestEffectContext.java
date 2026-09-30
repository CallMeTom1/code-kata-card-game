package com.arena.testing;

import com.arena.engine.combat.Combat;
import com.arena.engine.combat.DamageResolver;
import com.arena.engine.effects.EffectContext;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.CardDrawer;
import com.arena.engine.player.Champion;

/** Minimal context so effects can be tested without running a whole match. */
public final class TestEffectContext implements EffectContext {

    private final Champion caster;
    private final Champion opponent;
    private final EventPublisher events;
    private final Combat combat;
    private final CardDrawer drawer;
    private boolean combo;
    private int attackBonus;
    private int round = 1;
    private String source = "Test";

    private TestEffectContext(Champion caster, Champion opponent, EventPublisher events) {
        this.caster = caster;
        this.opponent = opponent;
        this.events = events;
        this.combat = new Combat(events);
        this.drawer = new CardDrawer(events);
    }

    /** Wires real combat and drawing on the given publisher, which is what effect tests need. */
    public static TestEffectContext between(Champion caster, Champion opponent, EventPublisher events) {
        return new TestEffectContext(caster, opponent, events);
    }

    public TestEffectContext withCombo() {
        this.combo = true;
        return this;
    }

    public TestEffectContext withAttackBonus(int bonus) {
        this.attackBonus = bonus;
        return this;
    }

    public TestEffectContext inRound(int round) {
        this.round = round;
        return this;
    }

    public TestEffectContext from(String source) {
        this.source = source;
        return this;
    }

    @Override
    public Champion caster() {
        return caster;
    }

    @Override
    public Champion opponent() {
        return opponent;
    }

    @Override
    public DamageResolver damage() {
        return combat;
    }

    @Override
    public EventPublisher events() {
        return events;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public boolean comboActive() {
        return combo;
    }

    @Override
    public int attackBonus() {
        return attackBonus;
    }

    @Override
    public int round() {
        return round;
    }

    @Override
    public void draw(Champion who, int count) {
        drawer.draw(who, count);
    }
}
