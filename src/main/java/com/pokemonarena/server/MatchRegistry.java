package com.pokemonarena.server;

import com.pokemonarena.bot.BotStrategy;
import com.pokemonarena.session.MatchSession;
import com.pokemonarena.session.PlayerSetup;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory Human vs AI sessions, lost when the server stops. The human is always side 1. */
public final class MatchRegistry {

    public static final int HUMAN_SIDE = 1;

    /** A running session with the buffer of its not-yet-delivered events. */
    public record Entry(String id, long seed, MatchSession session, EventBuffer events) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public Entry create(PlayerSetup human, PlayerSetup ai, BotStrategy aiBot, long seed) {
        EventBuffer events = new EventBuffer();
        MatchSession session = MatchSession.humanVsAi(human, ai, aiBot, seed, events);
        Entry entry = new Entry(UUID.randomUUID().toString(), seed, session, events);
        entries.put(entry.id(), entry);
        return entry;
    }

    public Optional<Entry> find(String id) {
        return Optional.ofNullable(entries.get(id));
    }
}
