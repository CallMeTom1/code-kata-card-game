package com.arena.engine.match;

/** What a bot can do during its play phase; closed list because the rules allow nothing else. */
public sealed interface Action permits PlayCard, UseHeroPower, EndTurn {
}
