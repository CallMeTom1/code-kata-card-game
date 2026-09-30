package com.arena.engine.board;

/** A minion on the board: its health changes, its template does not. */
public final class Minion {

    private final MinionTemplate template;
    private final int summonedRound;
    private int health;

    Minion(MinionTemplate template, int summonedRound) {
        this.template = template;
        this.summonedRound = summonedRound;
        this.health = template.health();
    }

    /** Name used in logs. */
    public String name() {
        return template.name();
    }

    /** Damage dealt when it attacks or is attacked by a minion. */
    public int attack() {
        return template.attack();
    }

    /** Current health; the minion dies at 0. */
    public int health() {
        return health;
    }

    /** Enemy minions must attack a Taunt minion first. */
    public boolean taunt() {
        return template.taunt();
    }

    /** Passive ability, if any. */
    public MinionAbility ability() {
        return template.ability();
    }

    /** Summoning sickness: never on the round it arrived, and never with 0 attack. */
    public boolean canAttack(int round) {
        return template.attack() > 0 && round > summonedRound;
    }

    /** Lowers health; the caller removes dead minions so it can publish the death. */
    public void takeDamage(int amount) {
        health -= amount;
    }

    /** True once health reaches 0. */
    public boolean isDead() {
        return health <= 0;
    }
}
