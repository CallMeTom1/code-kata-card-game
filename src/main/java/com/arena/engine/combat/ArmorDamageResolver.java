package com.arena.engine.combat;

import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** Applies armor first, then Parry, before removing HP; publishes the resulting {@link DamageDealt}. */
public final class ArmorDamageResolver implements DamageResolver {

    private final EventPublisher events;

    public ArmorDamageResolver(EventPublisher events) {
        this.events = events;
    }

    @Override
    public void deal(String source, Champion target, int amount) {
        int absorbedByArmor = target.absorbWithArmor(amount);
        int afterArmor = amount - absorbedByArmor;
        int parry = Math.min(afterArmor, target.parryAmount());
        int afterParry = afterArmor - parry;
        int totalAbsorbed = absorbedByArmor + parry;
        target.loseHp(afterParry);
        events.publish(new DamageDealt(source, target.name(), afterParry, totalAbsorbed, target.hp()));
    }

    @Override
    public void dealIgnoringArmor(String source, Champion target, int amount) {
        target.loseHp(amount);
        events.publish(new DamageDealt(source, target.name(), amount, 0, target.hp()));
    }
}
