package com.arena.log;

import com.arena.engine.events.CardBurned;
import com.arena.engine.events.CardDrawn;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.FatigueDamage;
import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;
import com.arena.engine.events.HeroPowerUsed;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MatchEnded;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.events.TurnStarted;

/** Prints a human-readable line for every event; the engine stays silent, this is just one listener. */
public final class ConsoleRenderer implements GameEventListener {

    @Override
    public void on(GameEvent event) {
        if (event instanceof MatchStarted started) {
            System.out.printf("=== Match: %s vs %s (seed %d) — %s goes first ===%n",
                    started.player1(), started.player2(), started.seed(), started.firstPlayer());
        } else if (event instanceof TurnStarted turn) {
            System.out.printf("--- Turn %d — %s ---%n", turn.turn(), turn.player());
        } else if (event instanceof CardDrawn drawn) {
            System.out.printf("  %s draws %s%n", drawn.player(), drawn.card());
        } else if (event instanceof CardBurned burned) {
            System.out.printf("  %s burns %s (hand full)%n", burned.player(), burned.card());
        } else if (event instanceof FatigueDamage fatigue) {
            System.out.printf("  %s takes %d fatigue damage → %d HP%n", fatigue.player(), fatigue.amount(), fatigue.hpLeft());
        } else if (event instanceof ManaRefilled mana) {
            System.out.printf("  %s now has %d max mana%n", mana.player(), mana.maxMana());
        } else if (event instanceof CardPlayed played) {
            System.out.printf("  %s plays %s (%d mana, %d left)%n", played.player(), played.card(), played.cost(), played.manaLeft());
        } else if (event instanceof HeroPowerUsed power) {
            System.out.printf("  %s uses %s (%d mana left)%n", power.player(), power.heroPower(), power.manaLeft());
        } else if (event instanceof DamageDealt dealt) {
            System.out.printf("  %s hits %s for %d (%d absorbed) → %s %d HP%n",
                    dealt.source(), dealt.target(), dealt.amount(), dealt.absorbed(), dealt.target(), dealt.targetHpLeft());
        } else if (event instanceof MatchEnded ended) {
            String winner = ended.winner() == null ? "a draw" : ended.winner() + " wins";
            System.out.printf("=== Match ended after %d turns: %s (%s) ===%n", ended.turns(), winner, ended.reason());
        }
    }
}
