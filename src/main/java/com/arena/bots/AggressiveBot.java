package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.GameView;

import java.util.List;
import java.util.Optional;

/**
 * Plays Resource cards first, then the highest-damage affordable Attack, repeating while mana allows;
 * uses the hero power with leftover mana.
 */
public final class AggressiveBot implements Bot {

    @Override
    public String name() {
        return "Aggressive";
    }

    @Override
    public Action nextAction(GameView view) {
        List<Card> hand = view.myHand();
        Optional<Integer> resource = bestIndexByCost(hand, view.myMana(), CardCategory.RESOURCE);
        if (resource.isPresent()) {
            return new Action.PlayCard(resource.get());
        }
        Optional<Integer> attack = bestAttack(hand, view.myMana());
        if (attack.isPresent()) {
            return new Action.PlayCard(attack.get());
        }
        if (view.heroPowerAvailable()) {
            return new Action.UseHeroPower();
        }
        return new Action.EndTurn();
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

    private Optional<Integer> bestAttack(List<Card> hand, int mana) {
        int bestIndex = -1;
        int bestCost = -1;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.category() == CardCategory.ATTACK && card.cost() <= mana && card.cost() > bestCost) {
                bestCost = card.cost();
                bestIndex = i;
            }
        }
        return bestIndex >= 0 ? Optional.of(bestIndex) : Optional.empty();
    }
}
