(function (root) {
  const { test, eq, throws } = root.Arena.Test;
  const S = () => root.Arena.Stats;

  function data(results) {
    return {
      player1: "Alice", label1: "Aggressive:Mage", player2: "Bob", label2: "Defensive:Tank", firstSeed: 42,
      summary: { matches: results.length, wins1: results.filter((r) => r.winner === "Alice").length,
        wins2: results.filter((r) => r.winner === "Bob").length, draws: results.filter((r) => !r.winner).length,
        winRate1: 0, winRate2: 0, drawRate: 0, firstPlayerWinRate: 0, averageRounds: 0,
        averageDamage1: 0, averageDamage2: 0, endReasons: {} },
      results,
    };
  }

  const r = (first, winner, rounds, reason) => ({ seed: 1, class1: "Mage", class2: "Tank", first, winner,
    reason: reason || "HP 0", rounds, hp1: 1, hp2: 1, damage1: 10, damage2: 10 });

  test("given_a_json_without_results_when_parsed_then_it_is_rejected", () => {
    throws(() => S().parse('{"player1":"A","player2":"B","summary":{}}'), "results");
  });

  test("given_matches_of_3_5_and_5_rounds_when_summarized_then_the_histogram_fills_the_gaps_with_zero", () => {
    const summary = S().summarize(data([r("Alice", "Alice", 3), r("Bob", "Bob", 5), r("Bob", "Alice", 5)]));
    eq(summary.lengths, [{ rounds: 3, count: 1 }, { rounds: 4, count: 0 }, { rounds: 5, count: 2 }]);
  });

  test("given_results_when_summarized_then_the_win_split_counts_both_sides_and_draws", () => {
    const summary = S().summarize(data([r("Alice", "Alice", 3), r("Bob", null, 50, "turn limit, full tie"), r("Bob", "Bob", 5)]));
    eq(summary.split.map((s) => [s.key, s.count]), [["p1", 1], ["draw", 1], ["p2", 1]]);
  });

  test("given_who_started_each_match_when_summarized_then_each_side_win_rate_is_split_by_starter", () => {
    const summary = S().summarize(data([r("Alice", "Alice", 3), r("Alice", "Bob", 3), r("Bob", "Alice", 4), r("Bob", "Alice", 4)]));
    eq(summary.byStarter, [
      { starter: "Alice", matches: 2, winRate1: 50, winRate2: 50 },
      { starter: "Bob", matches: 2, winRate1: 100, winRate2: 0 },
    ]);
  });

  test("given_end_reasons_when_summarized_then_they_are_sorted_by_count", () => {
    const summary = S().summarize(data([r("Alice", "Alice", 3), r("Bob", "Bob", 50, "turn limit, more HP"), r("Bob", "Bob", 4)]));
    eq(summary.reasons.map((x) => [x.reason, x.count]), [["HP 0", 2], ["turn limit, more HP", 1]]);
  });
})(typeof window !== "undefined" ? window : globalThis);
