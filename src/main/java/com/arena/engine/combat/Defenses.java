package com.arena.engine.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Armor, Parry and Evasion of one champion, kept apart from HP and mana (Single Responsibility). */
public final class Defenses {

    private final List<TimedAmount> armorLayers = new ArrayList<>();
    private final List<TimedAmount> parryLayers = new ArrayList<>();
    private int evasionTurns;

    /** Adds a layer that expires on its own; several layers stack, as in DESIGN.md. */
    public void addArmor(int amount, int turns) {
        armorLayers.add(new TimedAmount(amount, turns));
    }

    /** Total armor, shown in logs and used by Shield Slam. */
    public int armor() {
        return armorLayers.stream().mapToInt(layer -> layer.amount).sum();
    }

    /** Adds a per-hit reduction that expires like armor. */
    public void addParry(int amount, int turns) {
        parryLayers.add(new TimedAmount(amount, turns));
    }

    /** Total reduction applied to each hit after armor. */
    public int parry() {
        return parryLayers.stream().mapToInt(layer -> layer.amount).sum();
    }

    /** Arms the Evasion secret; a second cast only extends it. */
    public void grantEvasion(int turns) {
        evasionTurns = Math.max(evasionTurns, turns);
    }

    /** True while the Evasion secret is armed, so bots and logs can show it. */
    public boolean hasEvasion() {
        return evasionTurns > 0;
    }

    /** Spends the secret on the incoming damage; returns false when there was none. */
    public boolean consumeEvasion() {
        if (evasionTurns == 0) {
            return false;
        }
        evasionTurns = 0;
        return true;
    }

    /** Uses the layers that would expire first, so no armor is wasted; returns what was absorbed. */
    public int absorbWithArmor(int damage) {
        armorLayers.sort(Comparator.comparingInt(layer -> layer.turnsLeft));
        int remaining = damage;
        for (TimedAmount layer : armorLayers) {
            int used = Math.min(layer.amount, remaining);
            layer.amount -= used;
            remaining -= used;
        }
        armorLayers.removeIf(layer -> layer.amount == 0);
        return damage - remaining;
    }

    /** Ages every defense at the start of its owner's turn; returns the armor that expired. */
    public int onOwnerTurnStart() {
        int expiredArmor = age(armorLayers);
        age(parryLayers);
        evasionTurns = Math.max(0, evasionTurns - 1);
        return expiredArmor;
    }

    private static int age(List<TimedAmount> layers) {
        int expired = 0;
        for (TimedAmount layer : layers) {
            layer.turnsLeft--;
            if (layer.turnsLeft <= 0) {
                expired += layer.amount;
            }
        }
        layers.removeIf(layer -> layer.turnsLeft <= 0);
        return expired;
    }
}
