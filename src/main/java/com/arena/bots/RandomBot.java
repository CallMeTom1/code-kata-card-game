package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Baseline strategy: plays a random affordable card, or the hero power, or ends the turn. */
public final class RandomBot implements Bot {

    private final Random random;

    public RandomBot(Random random) {
        this.random = random;
    }

    @Override
    public String name() {
        return "Random";
    }

    @Override
    public Action nextAction(GameView view) {
        List<Integer> affordable = affordableHandIndexes(view);
        boolean canUseHeroPower = view.heroPowerAvailable();
        int choices = affordable.size() + (canUseHeroPower ? 1 : 0);
        if (choices == 0) {
            return new Action.EndTurn();
        }
        int pick = random.nextInt(choices + 1);
        if (pick < affordable.size()) {
            return new Action.PlayCard(affordable.get(pick));
        }
        if (canUseHeroPower && pick == affordable.size()) {
            return new Action.UseHeroPower();
        }
        return new Action.EndTurn();
    }

    private List<Integer> affordableHandIndexes(GameView view) {
        List<Integer> indexes = new ArrayList<>();
        List<Card> hand = view.myHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).cost() <= view.myMana()) {
                indexes.add(i);
            }
        }
        return indexes;
    }
}
