package com.arena.engine.combat;

import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.player.Champion;

/** Damage with no defense at all; the starting point before armor and Parry exist. */
public final class SimpleDamageResolver implements DamageResolver {

    private final EventPublisher events;

    /** Takes the publisher so every hit shows up in the log. */
    public SimpleDamageResolver(EventPublisher events) {
        this.events = events;
    }

    @Override
    public void deal(String source, Champion target, int amount) {
        target.loseHp(amount);
        events.publish(new DamageDealt(source, target.name(), amount, 0, target.hp()));
    }
}
