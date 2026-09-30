/*
 * Statistics view: tiles, four SVG charts (no library) and a table view.
 * Colors: player 1 = blue, player 2 = orange (validated for the dark surface), draws = neutral gray,
 * single-series charts = gold. Every mark has a hover tooltip; the table gives the same data as text.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const doc = root.document;
  const COLORS = { p1: "var(--p1)", p2: "var(--p2)", draw: "var(--neutral)", single: "var(--accent)" };

  const esc = (text) => String(text).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;" }[c]));
  const tr = Arena.I18n.t;
  const fmt = (n) => (Math.round(n * 10) / 10).toLocaleString(tr("fmt.locale"));
  const pct = (n) => tr("fmt.pct", { n: fmt(n) });

  function tile(label, value, sub, color) {
    return `<div class="tile"><div class="label">${color ? `<span class="swatch" style="background:${color}"></span>` : ""}${esc(label)}</div>
      <div class="value">${value}</div>${sub ? `<div class="sub">${sub}</div>` : ""}</div>`;
  }

  function legend(items) {
    return `<div class="legend">${items.map(([label, color]) =>
      `<span><span class="swatch" style="background:${color}"></span>${esc(label)}</span>`).join("")}</div>`;
  }

  /** The draw segment has no player name, so its label follows the page language. */
  const partLabel = (part) => (part.key === "draw" ? tr("stats.tile.draws") : part.label);

  /** 100% stacked bar: who won how often. */
  function splitChart(s) {
    const width = 600;
    const height = 52;
    let x = 0;
    const segments = s.split.filter((part) => part.count > 0).map((part) => {
      const w = (part.pct / 100) * width;
      const rect = `<rect class="mark" x="${x + 1}" y="0" width="${Math.max(0, w - 2)}" height="${height}" rx="4"
        fill="${COLORS[part.key]}" data-tip="${esc(tr("stats.split-tip", { label: partLabel(part), count: part.count, pct: pct(part.pct) }))}"></rect>`
        + (w > 70 ? `<text class="value-label" x="${x + w / 2}" y="${height / 2 + 6}" text-anchor="middle" style="fill:#fff">${pct(part.pct)}</text>` : "");
      x += w;
      return rect;
    }).join("");
    return `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="${esc(tr("stats.chart.split-aria"))}">${segments}</svg>`
      + legend([[s.player1, COLORS.p1], [tr("stats.tile.draws"), COLORS.draw], [s.player2, COLORS.p2]]);
  }

  /** Histogram of match lengths in rounds. */
  function lengthChart(s) {
    const width = 600;
    const height = 280;
    const m = { top: 24, right: 8, bottom: 44, left: 52 };
    const plotW = width - m.left - m.right;
    const plotH = height - m.top - m.bottom;
    const max = Math.max(1, ...s.lengths.map((b) => b.count));
    const step = plotW / Math.max(1, s.lengths.length);
    const ticks = [0, 0.25, 0.5, 0.75, 1].map((t) => Math.round(max * t));
    const grid = ticks.map((t) => {
      const y = m.top + plotH - (t / max) * plotH;
      return `<line class="grid" x1="${m.left}" x2="${width - m.right}" y1="${y}" y2="${y}"></line>
        <text x="${m.left - 6}" y="${y + 4}" text-anchor="end">${t}</text>`;
    }).join("");
    const peak = s.lengths.reduce((a, b) => (b.count > a.count ? b : a), s.lengths[0] || { count: 0 });
    const labelEvery = Math.ceil(s.lengths.length / 12);
    const bars = s.lengths.map((b, i) => {
      const h = (b.count / max) * plotH;
      const x = m.left + i * step + 1;
      const y = m.top + plotH - h;
      const share = s.matches ? (100 * b.count) / s.matches : 0;
      return `<rect class="mark" x="${x}" y="${y}" width="${Math.max(1, step - 2)}" height="${Math.max(0, h)}" rx="3"
          fill="${COLORS.single}" data-tip="${esc(tr("stats.chart.length-tip", { rounds: b.rounds, count: b.count, pct: pct(share) }))}"></rect>
        ${i % labelEvery === 0 ? `<text x="${x + step / 2 - 1}" y="${height - 22}" text-anchor="middle">${b.rounds}</text>` : ""}
        ${b === peak && b.count ? `<text class="value-label" x="${x + step / 2 - 1}" y="${y - 5}" text-anchor="middle">${b.count}</text>` : ""}`;
    }).join("");
    return `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="${esc(tr("stats.chart.length-aria"))}">
      ${grid}<line class="axis" x1="${m.left}" x2="${width - m.right}" y1="${m.top + plotH}" y2="${m.top + plotH}"></line>${bars}
      <text x="${m.left + plotW / 2}" y="${height - 2}" text-anchor="middle">${esc(tr("stats.chart.length-axis"))}</text></svg>`;
  }

  /** Grouped bars: each side's win rate depending on who started. */
  function starterChart(s) {
    const width = 600;
    const height = 280;
    const m = { top: 30, right: 8, bottom: 40, left: 64 };
    const plotW = width - m.left - m.right;
    const plotH = height - m.top - m.bottom;
    const groupW = plotW / s.byStarter.length;
    const barW = Math.min(90, groupW / 3);
    const grid = [0, 25, 50, 75, 100].map((t) => {
      const y = m.top + plotH - (t / 100) * plotH;
      return `<line class="grid" x1="${m.left}" x2="${width - m.right}" y1="${y}" y2="${y}"></line>
        <text x="${m.left - 6}" y="${y + 4}" text-anchor="end">${pct(t)}</text>`;
    }).join("");
    const bars = s.byStarter.map((g, i) => {
      const center = m.left + groupW * i + groupW / 2;
      return [["p1", g.winRate1, s.player1], ["p2", g.winRate2, s.player2]].map(([key, rate, name], j) => {
        const h = (rate / 100) * plotH;
        const x = center + (j === 0 ? -barW - 1 : 1);
        const y = m.top + plotH - h;
        return `<rect class="mark" x="${x}" y="${y}" width="${barW}" height="${Math.max(0, h)}" rx="4" fill="${COLORS[key]}"
            data-tip="${esc(tr("stats.starter-tip", { starter: g.starter, matches: g.matches, name, pct: pct(rate) }))}"></rect>
          <text class="value-label" x="${x + barW / 2}" y="${y - 6}" text-anchor="middle">${pct(rate)}</text>`;
      }).join("") + `<text x="${center}" y="${height - 10}" text-anchor="middle">${esc(tr("stats.starter-label", { starter: g.starter, matches: g.matches }))}</text>`;
    }).join("");
    return `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="${esc(tr("stats.chart.first-aria"))}">
      ${grid}<line class="axis" x1="${m.left}" x2="${width - m.right}" y1="${m.top + plotH}" y2="${m.top + plotH}"></line>${bars}</svg>`
      + legend([[s.player1, COLORS.p1], [s.player2, COLORS.p2]]);
  }

  /** Horizontal bars: how matches end. */
  function reasonChart(s) {
    const width = 600;
    const rowH = 40;
    const labelW = 230;
    const height = Math.max(1, s.reasons.length) * rowH;
    const plotW = width - labelW - 80;
    const rows = s.reasons.map((r, i) => {
      const y = i * rowH;
      const w = (r.pct / 100) * plotW;
      return `<text x="${labelW - 8}" y="${y + rowH / 2 + 6}" text-anchor="end">${esc(r.reason)}</text>
        <rect class="mark" x="${labelW}" y="${y + 6}" width="${Math.max(2, w)}" height="${rowH - 12}" rx="4" fill="${COLORS.single}"
          data-tip="${esc(tr("stats.chart.ends-tip", { reason: r.reason, count: r.count, pct: pct(r.pct) }))}"></rect>
        <text class="value-label" x="${labelW + Math.max(2, w) + 6}" y="${y + rowH / 2 + 6}">${pct(r.pct)}</text>`;
    }).join("");
    return `<svg viewBox="0 0 ${width} ${height}" role="img" aria-label="${esc(tr("stats.chart.ends-aria"))}">${rows}</svg>`;
  }

  function tableHtml(data) {
    const rows = data.results.slice(0, 1000).map((r) => `<tr><td>${r.seed}</td><td>${esc(r.class1)}</td><td>${esc(r.class2)}</td>
      <td>${esc(r.first)}</td><td>${r.winner ? esc(r.winner) : esc(tr("stats.table.draw"))}</td><td>${esc(r.reason)}</td><td>${r.rounds}</td>
      <td>${r.hp1}</td><td>${r.hp2}</td><td>${r.damage1}</td><td>${r.damage2}</td></tr>`).join("");
    return `<div class="table-wrap"><table class="data-table">
      <thead><tr><th>${tr("stats.table.seed")}</th><th>${esc(tr("stats.table.class", { player: data.player1 }))}</th>
      <th>${esc(tr("stats.table.class", { player: data.player2 }))}</th><th>${tr("stats.table.first")}</th>
      <th>${tr("stats.table.winner")}</th><th>${tr("stats.table.reason")}</th><th>${tr("stats.table.turns")}</th>
      <th>${esc(tr("stats.table.hp", { player: data.player1 }))}</th><th>${esc(tr("stats.table.hp", { player: data.player2 }))}</th>
      <th>${esc(tr("stats.table.damage", { player: data.player1 }))}</th><th>${esc(tr("stats.table.damage", { player: data.player2 }))}</th></tr></thead><tbody>${rows}</tbody></table></div>`
      + (data.results.length > 1000 ? `<p class="caption">${tr("stats.table.limit", { total: data.results.length })}</p>` : "");
  }

  function create() {
    const el = { root: doc.getElementById("stats"), empty: doc.getElementById("stats-empty"), tooltip: doc.getElementById("tooltip") };
    let data = null;

    function load(parsed) {
      data = parsed;
      render(false);
      el.empty.hidden = true;
      el.root.hidden = false;
    }

    /** Draws the whole view; a language change redraws it and keeps the table of matches open if it was. */
    function render(keepTable) {
      const tableOpen = keepTable && !!el.root.querySelector("#stats-table:not([hidden])");
      const s = Arena.Stats.summarize(data);
      const [p1, draw, p2] = s.split;
      el.root.innerHTML = `
        <div class="stats-head"><h2>${esc(s.player1)} [${esc(s.label1)}] vs ${esc(s.player2)} [${esc(s.label2)}]</h2>
          <p>${esc(tr("stats.summary", { matches: s.matches.toLocaleString(tr("fmt.locale")), from: s.firstSeed, to: s.firstSeed + s.matches - 1 }))}</p></div>
        <div class="tiles">
          ${tile(tr("stats.tile.wins", { player: s.player1 }), pct(p1.pct), tr("stats.count", { n: p1.count }), COLORS.p1)}
          ${tile(tr("stats.tile.wins", { player: s.player2 }), pct(p2.pct), tr("stats.count", { n: p2.count }), COLORS.p2)}
          ${tile(tr("stats.tile.draws"), pct(draw.pct), tr("stats.count", { n: draw.count }), COLORS.draw)}
          ${tile(tr("stats.tile.first"), pct(s.firstPlayerWinRate), tr("stats.tile.first-sub"))}
          ${tile(tr("stats.tile.length"), fmt(s.averageRounds), tr("stats.tile.length-sub"))}
          ${tile(tr("stats.tile.damage"), fmt(s.averageDamage1) + " · " + fmt(s.averageDamage2), esc(s.player1) + " · " + esc(s.player2))}
        </div>
        <div class="charts">
          <div class="chart-card"><h3>${tr("stats.chart.split")}</h3><p class="caption">${tr("stats.chart.split-caption", { matches: s.matches })}</p>${splitChart(s)}</div>
          <div class="chart-card"><h3>${tr("stats.chart.first")}</h3><p class="caption">${tr("stats.chart.first-caption")}</p>${starterChart(s)}</div>
          <div class="chart-card"><h3>${tr("stats.chart.length")}</h3><p class="caption">${tr("stats.chart.length-caption")}</p>${lengthChart(s)}</div>
          <div class="chart-card"><h3>${tr("stats.chart.ends")}</h3><p class="caption">${tr("stats.chart.ends-caption")}</p>${reasonChart(s)}</div>
        </div>
        <button class="btn table-toggle" data-action="toggle-table" aria-expanded="false">${tr("stats.table.show")}</button>
        <div id="stats-table" hidden></div>`;
      if (tableOpen) toggleTable();
    }

    function toggleTable() {
      const button = el.root.querySelector('[data-action="toggle-table"]');
      const table = doc.getElementById("stats-table");
      const open = table.hidden;
      if (open && !table.innerHTML) table.innerHTML = tableHtml(data);
      table.hidden = !open;
      button.setAttribute("aria-expanded", String(open));
      button.textContent = tr(open ? "stats.table.hide" : "stats.table.show");
    }

    el.root.addEventListener("click", (e) => {
      const button = e.target.closest('[data-action="toggle-table"]');
      if (button && data) toggleTable();
    });
    el.root.addEventListener("mousemove", (e) => {
      const mark = e.target.closest("[data-tip]");
      if (!mark) {
        el.tooltip.hidden = true;
        return;
      }
      el.tooltip.textContent = mark.getAttribute("data-tip");
      el.tooltip.hidden = false;
      const x = Math.min(e.clientX + 14, root.innerWidth - el.tooltip.offsetWidth - 8);
      el.tooltip.style.left = x + "px";
      el.tooltip.style.top = e.clientY + 14 + "px";
    });
    el.root.addEventListener("mouseleave", () => (el.tooltip.hidden = true));

    /** A new language rewrites every text of the loaded statistics. */
    function relabel() {
      if (data) render(true);
    }

    return { load, relabel, isLoaded: () => !!data };
  }

  Arena.StatsView = { create };
})(window);
