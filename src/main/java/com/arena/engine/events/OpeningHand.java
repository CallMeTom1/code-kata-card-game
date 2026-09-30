package com.arena.engine.events;

import java.util.List;

/** Hand after mulligan (and The Coin), before turn 1. */
public record OpeningHand(String player, List<String> cards) implements GameEvent {
}
