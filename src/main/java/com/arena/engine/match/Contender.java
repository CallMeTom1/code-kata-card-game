package com.arena.engine.match;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;

import java.util.List;

/**
 * One side before the match: name, bot, class and the 20 cards it brings.
 *
 * @param classChoice "imposed" or "auto", shown in the setup log
 * @param deckSource  "preset" or "built", shown in the setup log
 */
public record Contender(String name, Bot bot, HeroClass heroClass, List<Card> deck, String classChoice,
                        String deckSource) {

    /** Copies the deck so the match can never change the caller's list. */
    public Contender {
        deck = List.copyOf(deck);
    }

    /** "Aggressive:Mage", as typed on the command line. */
    public String label() {
        return bot.name() + ":" + heroClass.name();
    }
}
