package com.arena.engine.board;

/** Blueprint of a minion printed on a card; each summon creates a fresh {@link Minion}. */
public record MinionTemplate(String name, int attack, int health, boolean taunt, MinionAbility ability) {

    /** Shortcut for minions without a passive ability. */
    public MinionTemplate(String name, int attack, int health, boolean taunt) {
        this(name, attack, health, taunt, new MinionAbility.None());
    }
}
