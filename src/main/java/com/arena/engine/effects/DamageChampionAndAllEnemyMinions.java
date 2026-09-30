package com.arena.engine.effects;

import com.arena.engine.board.Minion;

/** Area effect (Whirlwind Slash, Holy Nova): the only way cards reach enemy minions. */
public record DamageChampionAndAllEnemyMinions(int amount) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        int damage = amount + ctx.attackBonus();
        ctx.damage().deal(ctx.source(), ctx.opponent(), damage);
        for (Minion minion : ctx.opponent().board().minions()) {
            ctx.damage().damageMinion(ctx.source(), ctx.opponent(), minion, damage);
        }
    }
}
