package com.arena.testing;

import com.arena.engine.cards.Card;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Test fake that replays a fixed list of actions, so engine tests control every decision. */
public class ScriptedBot implements Bot {

    private final String name;
    private final Deque<Action> script;

    public ScriptedBot(String name, Action... actions) {
        this.name = name;
        this.script = new ArrayDeque<>(List.of(actions));
    }

    @Override
    public String name() {
        return name;
    }

    /** Plays the next scripted action, then ends the turn forever once the script is used up. */
    @Override
    public Action nextAction(GameView view) {
        return script.isEmpty() ? new EndTurn() : script.poll();
    }

    @Override
    public List<Integer> mulligan(List<Card> openingHand) {
        return List.of();
    }
}
