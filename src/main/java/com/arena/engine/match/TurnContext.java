package com.arena.engine.match;

import com.arena.engine.combat.DamageResolver;
import com.arena.engine.effects.EffectContext;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.CardDrawer;
import com.arena.engine.player.Champion;

/** The EffectContext the match gives to each card or hero power it resolves. */
record TurnContext(Champion caster, Champion opponent, DamageResolver damage, EventPublisher events, String source,
                   boolean comboActive, int attackBonus, int round, CardDrawer drawer) implements EffectContext {

    @Override
    public void draw(Champion who, int count) {
        drawer.draw(who, count);
    }
}
