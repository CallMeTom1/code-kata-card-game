package com.arena.engine.match;

import com.arena.engine.cards.Card;
import com.arena.engine.player.Champion;

import java.util.List;

/** The only translation from mutable {@link Champion} state to the read-only view bots receive. */
public final class GameViewImpl implements GameView {

    private final int turn;
    private final Champion self;
    private final Champion opponent;

    public GameViewImpl(int turn, Champion self, Champion opponent) {
        this.turn = turn;
        this.self = self;
        this.opponent = opponent;
    }

    @Override
    public int turn() {
        return turn;
    }

    @Override
    public int myHp() {
        return self.hp();
    }

    @Override
    public int myArmor() {
        return self.totalArmor();
    }

    @Override
    public int myMana() {
        return self.mana();
    }

    @Override
    public List<Card> myHand() {
        return List.copyOf(self.hand());
    }

    @Override
    public boolean heroPowerAvailable() {
        return !self.heroPowerUsedThisTurn() && self.canAfford(self.heroClass().heroPower().cost());
    }

    @Override
    public int heroPowerCost() {
        return self.heroClass().heroPower().cost();
    }

    @Override
    public int opponentHp() {
        return opponent.hp();
    }

    @Override
    public int opponentArmor() {
        return opponent.totalArmor();
    }

    @Override
    public int opponentHandSize() {
        return opponent.hand().size();
    }
}
