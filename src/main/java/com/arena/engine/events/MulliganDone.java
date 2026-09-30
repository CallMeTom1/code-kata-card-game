package com.arena.engine.events;

import java.util.List;

/** Cards a bot put back in its deck and what it drew instead. */
public record MulliganDone(String player, List<String> putBack, List<String> drawn) implements GameEvent {
}
