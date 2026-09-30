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

    /** Cost of the hero power, so bots can plan their mana. */
    int heroPowerCost();

    /** Number of own minions, to avoid summoning onto a full board. */
    int myBoardSize();

    /** Number of enemy minions, to value area effects. */
    int opponentBoardSize();

    /** Own class name, so a bot can reason about its hero power and class cards. */
    String myClass();

    /** Enemy class name: public in Hearthstone, and it tells which cards to expect. */
    String opponentClass();

    /** Own max mana, to plan the next turns. */
    int myMaxMana();

    /** Cards left in the own deck, to anticipate fatigue. */
    int myDeckSize();

    /** Own minions in attack order, to judge the board. */
    List<MinionView> myMinions();

    /** Enemy minions in board order: Taunts and threats are public information. */
    List<MinionView> opponentMinions();

    /** Last thing the opponent said ("" if nothing), so talking bots can answer each other. */
    String opponentLastWords();
}
