package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.GameView;

import java.util.List;
import java.util.Optional;

/**
 * Above 15 HP behaves like {@link AggressiveBot}; at 15 HP or below it plays Utility (heal) then
 * the largest Defense card first, attacking only with mana left over.
 */
public final class DefensiveBot implements Bot {

    public static final int LOW_HP_THRESHOLD = 15;

    private final AggressiveBot aggressive = new AggressiveBot();

    @Override
    public String name() {
        return "Defensive";
    }

    @Override
    public Action nextAction(GameView view) {
        if (view.myHp() > LOW_HP_THRESHOLD) {
            return aggressive.nextAction(view);
        }
        List<Card> hand = view.myHand();
        Optional<Integer> utility = bestIndexByCost(hand, view.myMana(), CardCategory.UTILITY);
        if (utility.isPresent()) {
            return new Action.PlayCard(utility.get());
        }
        Optional<Integer> defense = bestIndexByCost(hand, view.myMana(), CardCategory.DEFENSE);
        if (defense.isPresent()) {
            return new Action.PlayCard(defense.get());
        }
        return aggressive.nextAction(view);
    }

    private Optional<Integer> bestIndexByCost(List<Card> hand, int mana, CardCategory category) {
        int bestIndex = -1;
        int bestCost = -1;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.category() == category && card.cost() <= mana && card.cost() > bestCost) {
                bestCost = card.cost();
                bestIndex = i;
            }
        }
        return bestIndex >= 0 ? Optional.of(bestIndex) : Optional.empty();
    }
}
