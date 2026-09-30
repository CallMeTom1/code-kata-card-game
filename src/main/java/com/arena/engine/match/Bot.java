package com.arena.engine.match;

/** A pure strategy: reads a {@link GameView} and returns an {@link Action}. No IO, no engine access. */
public interface Bot {

    String name();

    Action nextAction(GameView view);
}
