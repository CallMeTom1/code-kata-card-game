package com.arena.engine.classes;

import com.arena.engine.cards.Card;

import java.util.List;

/** A playable class: its hero power plus its 20-card deck (class cards x2 + neutral filler). */
public record HeroClass(String name, HeroPower heroPower, List<Card> deck) {
}
