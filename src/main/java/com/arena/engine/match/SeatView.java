package com.arena.engine.match;

import com.arena.engine.cards.Card;

import java.util.List;

/** Immutable snapshot handed to a bot; bots never touch the live champions. */
record SeatView(int turn, int myHp, int myArmor, int myMana, List<Card> myHand, boolean heroPowerAvailable,
                int heroPowerCost, int myBoardSize, int opponentHp, int opponentArmor, int opponentHandSize,
                int opponentBoardSize) implements GameView {
}
