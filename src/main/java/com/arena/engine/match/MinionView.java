package com.arena.engine.match;

/** What a bot may know about a minion: public on the board in Hearthstone, and immutable here. */
public record MinionView(String name, int attack, int health, boolean taunt, boolean canAttack) {
}
