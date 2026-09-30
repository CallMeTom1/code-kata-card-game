package com.arena.engine.classes;

import com.arena.engine.effects.Effect;

/** A class's signature ability: 2 mana, usable once per turn during the play phase. */
public record HeroPower(String name, int cost, Effect effect) {
}
