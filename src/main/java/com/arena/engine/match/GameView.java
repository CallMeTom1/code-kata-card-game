package com.arena.engine.match;

import com.arena.engine.cards.Card;

import java.util.List;

/** Read-only snapshot a {@link Bot} sees; it never gets the mutable engine state (Interface Segregation). */
public interface GameView {

    int turn();

    int myHp();

    int myArmor();

    int myMana();

    List<Card> myHand();

    boolean heroPowerAvailable();

    int heroPowerCost();

    int opponentHp();

    int opponentArmor();

    int opponentHandSize();
}
