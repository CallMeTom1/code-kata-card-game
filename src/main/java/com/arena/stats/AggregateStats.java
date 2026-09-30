package com.arena.stats;

/** Summary of a batch of matches, printed once at the end of a run. */
public record AggregateStats(int matches, int player1Wins, int player2Wins, int draws,
                              int firstPlayerWins, double averageTurns,
                              double averageDamageByPlayer1, double averageDamageByPlayer2) {

    public double player1WinRate() {
        return matches == 0 ? 0 : (double) player1Wins / matches;
    }

    public double player2WinRate() {
        return matches == 0 ? 0 : (double) player2Wins / matches;
    }

    public double drawRate() {
        return matches == 0 ? 0 : (double) draws / matches;
    }

    public double firstPlayerWinRate() {
        return matches == 0 ? 0 : (double) firstPlayerWins / matches;
    }
}
