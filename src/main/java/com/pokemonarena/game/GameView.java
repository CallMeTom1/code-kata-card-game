package com.pokemonarena.game;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a match, taken when the view is created, for a decision-maker (Bot or
 * human UI): its own state, the opponent's state, the turn state and the legal actions. It
 * holds no reference to the live match, so it cannot mutate it; decisions are returned as
 * {@link Action}s and submitted to {@link Match#perform(Action)}. After an action is performed
 * the view is stale: build a new one.
 */
public final class GameView {

    private final PlayerView self;
    private final PlayerView opponent;
    private final int activeSide;
    private final Phase phase;
    private final int turnNumber;
    private final boolean over;
    private final List<Action> legalActions;

    public GameView(Match match) {
        this(Objects.requireNonNull(match, "match"), match.side(match.active()));
    }

    private GameView(Match match, int side) {
        if (side != 1 && side != 2) {
            throw new IllegalArgumentException("side must be 1 or 2, not " + side);
        }
        PlayerState selfState = side == 1 ? match.player1() : match.player2();
        PlayerState opponentState = side == 1 ? match.player2() : match.player1();
        this.activeSide = match.side(match.active());
        this.self = PlayerView.of(side, selfState);
        this.opponent = PlayerView.of(3 - side, opponentState);
        this.phase = match.phase();
        this.turnNumber = match.turnNumber();
        this.over = match.isOver();
        this.legalActions = activeSide == side ? List.copyOf(match.legalActions()) : List.of();
    }

    /**
     * Snapshot seen from a fixed side (e.g. the human's), whoever is active. The legal actions
     * are empty while that side cannot act.
     */
    public static GameView forSide(Match match, int side) {
        return new GameView(Objects.requireNonNull(match, "match"), side);
    }

    /** The side this view is seen from (the side that must act, unless built with {@link #forSide}). */
    public PlayerView self() {
        return self;
    }

    public PlayerView opponent() {
        return opponent;
    }

    public int activeSide() {
        return activeSide;
    }

    public Phase phase() {
        return phase;
    }

    public int turnNumber() {
        return turnNumber;
    }

    public boolean isOver() {
        return over;
    }

    public List<Action> legalActions() {
        return legalActions;
    }
}
