package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.match.GameView;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/** Small helpers shared by the bots to pick a card position in the hand. */
final class HandChoices {

    private HandChoices() {
    }

    /** Position of the best affordable card matching the filter, by the given order. */
    static OptionalInt best(GameView view, Predicate<Card> filter, Comparator<Card> order) {
        List<Card> hand = view.myHand();
        return IntStream.range(0, hand.size())
                .filter(i -> hand.get(i).cost() <= view.myMana() && filter.test(hand.get(i)))
                .boxed()
                .max(Comparator.comparing(hand::get, order))
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    /** True when some card other than {@code except} matches and fits in the given mana. */
    static boolean anyOther(GameView view, Card except, int mana, Predicate<Card> filter) {
        List<Card> hand = view.myHand();
        boolean skipped = false;
        for (Card card : hand) {
            if (!skipped && card == except) {
                skipped = true;
                continue;
            }
            if (card.cost() <= mana && filter.test(card)) {
                return true;
            }
        }
        return false;
    }
}
