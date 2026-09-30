/* Statistics model for the Statistics tab: reads --stats-json output, computes what the charts show. */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const pct = (part, total) => (total ? Math.round((1000 * part) / total) / 10 : 0);

  /** Parses and checks the file, so a wrong file gives a clear message instead of an empty tab. */
  function parse(text) {
    let data;
    try {
      data = JSON.parse(text);
    } catch (e) {
      throw new Error("Invalid stats file: " + e.message);
    }
    for (const field of ["player1", "player2", "summary", "results"]) {
      if (!data || data[field] === undefined) throw new Error("Invalid stats file: missing " + field);
    }
    if (!Array.isArray(data.results)) throw new Error("Invalid stats file: results must be a list");
    return data;
  }

  function summarize(data) {
    const { player1, player2, results } = data;
    const total = results.length;
    const wins1 = results.filter((r) => r.winner === player1).length;
    const wins2 = results.filter((r) => r.winner === player2).length;
    const draws = total - wins1 - wins2;

    const rounds = results.map((r) => r.rounds);
    const min = rounds.length ? Math.min(...rounds) : 0;
    const max = rounds.length ? Math.max(...rounds) : -1;
    const lengths = [];
    for (let n = min; n <= max; n++) lengths.push({ rounds: n, count: rounds.filter((x) => x === n).length });

    const byStarter = [player1, player2].map((starter) => {
      const subset = results.filter((r) => r.first === starter);
      return { starter, matches: subset.length,
        winRate1: pct(subset.filter((r) => r.winner === player1).length, subset.length),
        winRate2: pct(subset.filter((r) => r.winner === player2).length, subset.length) };
    });

    const reasonCounts = new Map();
    results.forEach((r) => reasonCounts.set(r.reason, (reasonCounts.get(r.reason) || 0) + 1));
    const reasons = [...reasonCounts].map(([reason, count]) => ({ reason, count, pct: pct(count, total) }))
      .sort((a, b) => b.count - a.count || a.reason.localeCompare(b.reason));

    const average = (key) => (total ? Math.round((10 * results.reduce((s, r) => s + r[key], 0)) / total) / 10 : 0);
    return {
      player1, player2, label1: data.label1, label2: data.label2, firstSeed: data.firstSeed, matches: total,
      split: [
        { key: "p1", label: player1, count: wins1, pct: pct(wins1, total) },
        { key: "draw", label: "Draws", count: draws, pct: pct(draws, total) },
        { key: "p2", label: player2, count: wins2, pct: pct(wins2, total) },
      ],
      firstPlayerWinRate: pct(results.filter((r) => r.winner && r.winner === r.first).length, total),
      averageRounds: average("rounds"), averageDamage1: average("damage1"), averageDamage2: average("damage2"),
      lengths, byStarter, reasons,
    };
  }

  Arena.Stats = { parse, summarize };
})(typeof window !== "undefined" ? window : globalThis);
