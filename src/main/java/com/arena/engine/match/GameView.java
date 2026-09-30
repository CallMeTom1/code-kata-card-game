package com.arena.engine.match;

import com.arena.engine.cards.Card;

import java.util.List;

/** Read-only snapshot given to bots, so no strategy can change the game state (Interface Segregation). */
public interface GameView {

    /** Current round, so bots can plan around the 50-turn limit. */
    int turn();

    /** Own HP, used by the Defensive bot to switch mode. */
    int myHp();

    /** Own armor, useful for cards like Shield Slam. */
    int myArmor();

    /** Mana left this turn, to know what is affordable. */
    int myMana();

    /** Own hand in order; {@link PlayCard} refers to these positions. */
    List<Card> myHand();

    /** True when the hero power is unused this turn and affordable. */
    boolean heroPowerAvailable();

    /** Enemy HP, to spot lethal. */
    int opponentHp();

    /** Enemy armor, to estimate real damage. */
    int opponentArmor();

    /** Only the size of the enemy hand: bots must not see hidden cards. */
    int opponentHandSize();
}
