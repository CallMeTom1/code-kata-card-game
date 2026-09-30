package com.arena.engine.match;

import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;
import com.arena.engine.events.PoisonTicked;

import java.util.HashMap;
import java.util.Map;

/** Counts the HP each champion lost to the enemy (cards, minions, poison, hero power; not fatigue). */
final class DamageTracker implements GameEventListener {

    private final Map<String, Integer> hpLost = new HashMap<>();

    @Override
    public void on(GameEvent event) {
        switch (event) {
            case DamageDealt hit -> add(hit.target(), hit.hpBefore() - hit.hpAfter());
            case PoisonTicked tick -> add(tick.player(), tick.hpBefore() - tick.hpAfter());
            default -> {
            }
        }
    }

    /** HP the given champion lost, i.e. the damage its opponent dealt. */
    int hpLostBy(String player) {
        return hpLost.getOrDefault(player, 0);
    }

    private void add(String player, int amount) {
        hpLost.merge(player, amount, Integer::sum);
    }
}
