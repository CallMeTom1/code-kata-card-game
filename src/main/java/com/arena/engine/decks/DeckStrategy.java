package com.arena.engine.decks;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;

import java.util.List;

/** How a bot prepares for a match, kept apart from how it plays (Interface Segregation). */
public interface DeckStrategy {

    /** The class the bot picks when the command line says {@code auto}. */
    HeroClass chooseClass(HeroClasses classes);

    /** A legal 20-card deck from the class cards and the neutral pool. */
    List<Card> buildDeck(HeroClass heroClass, List<Card> neutralCards);
}
