package com.arena.engine.effects;

import com.arena.engine.board.Minion;
import com.arena.engine.board.MinionTemplate;
import com.arena.engine.events.MinionSummoned;
import com.arena.engine.events.SummonFizzled;

import java.util.Optional;

/** Puts minions on the caster board; each one that does not fit fizzles. */
public record Summon(MinionTemplate template, int count) implements Effect {

    @Override
    public void apply(EffectContext ctx) {
        for (int i = 0; i < count; i++) {
            Optional<Minion> minion = ctx.caster().board().summon(template, ctx.round());
            if (minion.isPresent()) {
                ctx.events().publish(new MinionSummoned(ctx.caster().name(), template.name(), template.attack(),
                        template.health(), template.taunt(), ctx.caster().board().minions().size()));
            } else {
                ctx.events().publish(new SummonFizzled(ctx.caster().name(), template.name()));
            }
        }
    }
}
