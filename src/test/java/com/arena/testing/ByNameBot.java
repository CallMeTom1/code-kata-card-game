package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.UseHeroPower;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Test bot following a plan of card names, "POWER" and "END" (one token per decision), so
 * engine tests do not depend on the shuffled order of the hand.
 */
public final class ByNameBot implements Bot {

    public static final String POWER = "POWER";
    public static final String END = "END";

    private final String name;
    private final Deque<String> plan;

    public ByNameBot(String name, String... plan) {
        this.name = name;
        this.plan = new ArrayDeque<>(List.of(plan));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Action nextAction(GameView view) {
        String next = plan.poll();
        if (next == null || next.equals(END)) {
            return new EndTurn();
        }
        if (next.equals(POWER)) {
            return new UseHeroPower();
        }
        List<Card> hand = view.myHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).name().equals(next)) {
                return new PlayCard(i);
            }
        }
        return new EndTurn();
    }
}
