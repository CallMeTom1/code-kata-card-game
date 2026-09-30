package com.arena.testing;

import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.GameView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Test fake bot that replays a fixed list of actions per turn, then always ends the turn. */
public final class ScriptedBot implements Bot {

    private final String name;
    private final Deque<Deque<Action>> turns;

    public ScriptedBot(String name, List<List<Action>> plannedTurns) {
        this.name = name;
        this.turns = new ArrayDeque<>();
        for (List<Action> turn : plannedTurns) {
            this.turns.add(new ArrayDeque<>(turn));
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Action nextAction(GameView view) {
        if (turns.isEmpty()) {
            return new Action.EndTurn();
        }
        Deque<Action> currentTurn = turns.peekFirst();
        if (currentTurn.isEmpty()) {
            turns.pollFirst();
            return new Action.EndTurn();
        }
        return currentTurn.pollFirst();
    }
}
