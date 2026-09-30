package com.arena.engine.combat;

import com.arena.engine.board.Minion;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EvasionTriggered;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.MinionDamaged;
import com.arena.engine.events.MinionDied;
import com.arena.engine.player.Champion;

/** Applies the defensive rules of DESIGN.md and reports every hit. */
public final class Combat implements DamageResolver {

    private final EventPublisher events;

    /** Takes the publisher so every hit shows up in the log. */
    public Combat(EventPublisher events) {
        this.events = events;
    }

    @Override
    public void deal(String source, Champion target, int amount) {
        hit(source, target, amount, false);
    }

    @Override
    public void dealIgnoringArmor(String source, Champion target, int amount) {
        hit(source, target, amount, true);
    }

    @Override
    public void damageMinion(String source, Champion owner, Minion minion, int amount) {
        if (amount <= 0) {
            return;
        }
        minion.takeDamage(amount);
        events.publish(new MinionDamaged(owner.name(), minion.name(), source, amount, Math.max(0, minion.health())));
        if (minion.isDead()) {
            owner.board().remove(minion);
            events.publish(new MinionDied(owner.name(), minion.name()));
        }
    }

    private void hit(String source, Champion target, int amount, boolean ignoreArmor) {
        if (amount <= 0) {
            return;
        }
        if (target.defenses().consumeEvasion()) {
            events.publish(new EvasionTriggered(target.name(), source, amount));
            return;
        }
        int hpBefore = target.hp();
        int absorbedByArmor = ignoreArmor ? 0 : target.defenses().absorbWithArmor(amount);
        int passing = Math.max(0, amount - absorbedByArmor - target.defenses().parry());
        target.loseHp(passing);
        events.publish(new DamageDealt(source, target.name(), amount, amount - passing, hpBefore, target.hp(),
                target.defenses().armor()));
    }
}
