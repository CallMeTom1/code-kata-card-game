package com.pokemonarena.bot;

import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;

/**
 * Strategy pattern: a Bot only decides which {@link Action} comes next. It never mutates the
 * game state; the engine validates and applies the action. A human player is simply another
 * producer of Actions, so the engine does not know whether a side is a Bot or a human.
 */
public interface BotStrategy {

    String name();

    /** Chooses one of {@link GameView#legalActions()}; returning EndTurn stops the turn. */
    Action chooseAction(GameView view);
}
