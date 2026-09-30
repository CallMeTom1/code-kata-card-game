package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.match.GameView;

import java.util.List;

/** Builds the read-only view a bot receives, so bot tests need no match. */
public final class ViewBuilder {

    private int hp = 30;
    private int mana;
    private List<Card> hand = List.of();
    private boolean heroPower;
    private int boardSize;

    private ViewBuilder() {
    }

    public static ViewBuilder aView() {
        return new ViewBuilder();
    }

    public ViewBuilder withHp(int hp) {
        this.hp = hp;
        return this;
    }

    public ViewBuilder withMana(int mana) {
        this.mana = mana;
        return this;
    }

    public ViewBuilder withHand(Card... cards) {
        this.hand = List.of(cards);
        return this;
    }

    public ViewBuilder withHeroPowerAvailable() {
        this.heroPower = true;
        return this;
    }

    public ViewBuilder withBoardSize(int size) {
        this.boardSize = size;
        return this;
    }

    public GameView build() {
        return new Snapshot(hp, mana, hand, heroPower, boardSize);
    }

    private record Snapshot(int myHp, int myMana, List<Card> myHand, boolean heroPowerAvailable, int myBoardSize)
            implements GameView {
        @Override
        public int turn() {
            return 1;
        }

        @Override
        public int myArmor() {
            return 0;
        }

        @Override
        public int opponentHp() {
            return 30;
        }

        @Override
        public int opponentArmor() {
            return 0;
        }

        @Override
        public int opponentHandSize() {
            return 4;
        }

        @Override
        public int heroPowerCost() {
            return 2;
        }

        @Override
        public int opponentBoardSize() {
            return 0;
        }
    }
}
