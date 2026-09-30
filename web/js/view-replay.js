/*
 * Replay view: draws one frame of the timeline (board, hands, heroes, minions, log) and drives playback.
 * All game logic lives in replay.js; this file only reads frames and writes DOM.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const doc = root.document;

  const tr = Arena.I18n.t;
  const CATEGORIES = ["ATTACK", "DEFENSE", "RESOURCE", "UTILITY"];
  const categoryShort = (category) => tr("cat.short." + category, null, "?");
  const BASE_DELAY_MS = 900;

  /** The design theme is read from the page, so switching it only needs a redraw. */
  const art = (kind, name, category) => Arena.Themes.artHtml(doc.documentElement.dataset.theme, kind, name, category);
  const esc = (text) => String(text).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;" }[c]));

  function cardHtml(card, isNew) {
    return `<div class="card${isNew ? " is-new" : ""}" tabindex="0" data-cat="${esc(card.category)}" data-name="${esc(card.name)}"
        data-cost="${card.cost}" data-text="${esc(card.text || "")}" aria-label="${esc(tr("card.aria", { name: card.name, cost: card.cost, text: card.text || "" }))}">
      <div class="cost"><span>${card.cost}</span></div>
      <div class="card-icon" aria-hidden="true">${art("card", card.name, card.category)}</div>
      <div class="card-name">${esc(card.name)}</div>
      <div class="card-cat">${categoryShort(card.category)}</div>
    </div>`;
  }

  function badges(p) {
    const list = [];
    if (p.frozen) list.push(tr("badge.frozen"));
    if (p.frozenThisTurn) list.push(tr("badge.frozen-turn"));
    p.poisons.forEach((x) => list.push(tr("badge.poison", { amount: x.amount, turns: x.turns })));
    if (p.evasion) list.push(tr("badge.evasion"));
    if (p.parry) list.push(tr("badge.parry", { n: p.parry }));
    if (p.attackBuff) list.push(tr("badge.attack-buff", { n: p.attackBuff }));
    if (p.fatigue) list.push(tr("badge.fatigue", { n: p.fatigue }));
    return list.map((b) => `<span class="badge">${esc(b)}</span>`).join("");
  }

  function manaHtml(p) {
    let gems = "";
    for (let i = 0; i < 10; i++) {
      const cls = i < p.mana ? "is-full" : i < p.maxMana ? "is-spent" : "";
      gems += `<span class="crystal ${cls}"></span>`;
    }
    return `<div class="mana" title="${esc(tr("mana.title", { mana: p.mana, max: p.maxMana }))}">${gems}<span>&nbsp;${p.mana}/${p.maxMana}</span></div>`;
  }

  function sideHtml(state, name, highlight) {
    const p = state.players[name];
    const current = state.current === name && state.phase === "play";
    const dead = state.phase === "ended" && p.hp === 0;
    const powerUsed = highlight && highlight.kind === "power" && highlight.player === name;
    return `<div class="side" data-player="${esc(name)}">
      <div class="side-info">
        <div class="hero-name">${esc(name)}</div>
        <div class="hero-label">${esc(p.bot)} · ${esc(p.heroClass)}</div>
        <div class="deck-pile" title="${esc(tr("deck.title"))}">${esc(tr("deck.pile", { deck: p.deckCount, hand: p.hand.length }))}</div>
        <div class="badges">${badges(p)}</div>
      </div>
      <div class="hero-block">
        <div class="hero${current ? " is-current" : ""}${dead ? " is-dead" : ""}" data-class="${esc(p.heroClass)}" data-hero="${esc(name)}" title="${esc(tr("hero.title", { name, hp: p.hp, armor: p.armor }))}">
          <span class="hero-art" aria-hidden="true">${art("hero", p.heroClass)}</span>
          ${p.armor > 0 ? `<div class="gem gem-armor" title="${esc(tr("hero.armor"))}">${p.armor}</div>` : ""}
          <div class="gem gem-hp" title="${esc(tr("hero.hp"))}">${p.hp}</div>
        </div>
        <div class="power${powerUsed ? " is-used" : ""}" title="${esc(tr("hero.power", { power: p.heroPower, text: p.heroPowerText }))}">
          <div class="cost"><span>${p.heroPowerCost}</span></div>${esc(p.heroPower)}
        </div>
      </div>
      <div class="side-info right">${manaHtml(p)}</div>
    </div>`;
  }

  function handHtml(state, name, highlight) {
    const p = state.players[name];
    const lastDrawn = highlight && highlight.kind === "draw" && highlight.player === name ? p.hand.length - 1 : -1;
    return `<div class="hand" data-player="${esc(name)}" aria-label="${esc(tr("hand.aria", { name }))}">${p.hand.map((c, i) => cardHtml(c, i === lastDrawn)).join("")}</div>`;
  }

  function minionsHtml(state, name, highlight) {
    const p = state.players[name];
    const acting = highlight && highlight.kind === "minion-attack" ? highlight.minionId : null;
    const hit = highlight && highlight.kind === "minion-damage" ? highlight.minionId : null;
    return `<div class="minions" aria-label="${esc(tr("board.aria", { name }))}">${p.board.map((m) => `
      <div class="minion${m.taunt ? " is-taunt" : ""}${m.id === acting ? " is-acting" : ""}${m.id === hit ? " is-hit" : ""}" data-minion-id="${m.id}" title="${esc(m.name)} ${m.attack}/${m.health}${m.taunt ? " — " + esc(tr("minion.taunt")) : ""}">
        <span class="minion-icon" aria-hidden="true">${art("card", m.name)}</span>${esc(m.name)}
        <div class="stat atk">${m.attack}</div>
        <div class="stat hp${m.health < m.maxHealth ? " is-hurt" : ""}">${m.health}</div>
      </div>`).join("")}</div>`;
  }

  function describe(event) {
    switch (event.type) {
      case "CardPlayed": return [event.player, tr("action.card-played", { card: esc(event.card), cost: event.cost })];
      case "HeroPowerUsed": return [event.player, tr("action.hero-power", { power: esc(event.power) })];
      case "DamageDealt": return [event.source, tr("action.damage", { amount: event.hpBefore - event.hpAfter, target: esc(event.target) })
        + (event.absorbed ? tr("action.absorbed", { n: event.absorbed }) : "")];
      case "Healed": return [event.source, tr("action.heal", { player: esc(event.player), amount: event.amount })];
      case "ArmorGained": return [event.source, tr("action.armor", { amount: event.amount, player: esc(event.player) })];
      case "MinionSummoned": return [event.owner, tr("action.summon", { minion: esc(event.minion), attack: event.attack, health: event.health })
        + (event.taunt ? tr("action.summon-taunt") : "")];
      case "MinionAttacked": return [event.owner, tr("action.minion-attack", { minion: esc(event.minion), target: esc(event.target) })];
      case "MinionDied": return [event.owner, tr("action.minion-died", { minion: esc(event.minion) })];
      case "StatusApplied": return [event.source, tr("action.status", { target: esc(event.target), status: esc(event.status) })];
      case "EvasionTriggered": return [event.player, tr("action.evasion", { amount: event.prevented, source: esc(event.source) })];
      case "PoisonTicked": return [event.player, tr("action.poison", { amount: event.amount })];
      case "FatigueDamage": return [event.player, tr("action.fatigue", { amount: event.amount })];
      case "CardBurned": return [event.player, tr("action.burn", { card: esc(event.card) })];
      case "IllegalAction": return [event.player, tr("action.illegal", { reason: esc(event.reason) })];
      case "ManaGained": return [event.source, tr("action.mana", { mana: event.mana, max: event.maxMana })];
      default: return null;
    }
  }

  function floater(event) {
    switch (event.type) {
      case "DamageDealt": return event.hpBefore > event.hpAfter
        ? { target: event.target, text: "−" + (event.hpBefore - event.hpAfter), kind: "damage" }
        : { target: event.target, text: "🛡", kind: "armor" };
      case "PoisonTicked": case "FatigueDamage": return { target: event.player, text: "−" + event.amount, kind: "damage" };
      case "Healed": return event.amount ? { target: event.player, text: "+" + event.amount, kind: "heal" } : null;
      case "ArmorGained": return { target: event.player, text: "+" + event.amount + " 🛡", kind: "armor" };
      default: return null;
    }
  }

  function curveBars(deck) {
    const buckets = [0, 0, 0, 0, 0, 0];
    deck.forEach((c) => buckets[Math.min(c.cost, 5)]++);
    const max = Math.max(1, ...buckets);
    return `<div class="curve" aria-label="${esc(tr("curve.aria"))}">${buckets.map((n, cost) =>
      `<div style="height:${Math.round((n / max) * 100)}%" title="${esc(tr("curve.bar", { n, cost: cost === 5 ? "5+" : cost }))}"><em>${n}</em><span>${cost === 5 ? "5+" : cost}</span></div>`).join("")}</div>`;
  }

  function deckGroups(deck) {
    return CATEGORIES.map((cat) => {
      const counts = new Map();
      deck.filter((c) => c.category === cat).forEach((c) => counts.set(c.name, (counts.get(c.name) || 0) + 1));
      if (!counts.size) return "";
      const total = [...counts.values()].reduce((a, b) => a + b, 0);
      return `<div class="deck-group" data-cat="${cat}"><b>${categoryShort(cat)} ${total}</b>${[...counts].map(([n, k]) => `${esc(n)} ×${k}`).join(", ")}</div>`;
    }).join("");
  }

  function setupHtml(state) {
    const players = state.order.map((name, i) => {
      const p = state.players[name];
      const mull = p.mulligan && p.mulligan.putBack.length
        ? tr("setup.mulligan-swap", { putBack: p.mulligan.putBack.map(esc).join(", "), drawn: p.mulligan.drawn.map(esc).join(", ") })
        : tr("setup.mulligan-keep");
      return `<div class="setup-player">
        <h3><span class="swatch" style="background:var(--p${i + 1})"></span><span class="setup-art">${art("hero", p.heroClass)}</span> ${esc(name)}</h3>
        <dl>
          <dt>${tr("setup.bot")}</dt><dd>${esc(p.bot)}</dd>
          <dt>${tr("setup.class")}</dt><dd>${esc(p.heroClass)} (${tr(p.classChoice === "auto" ? "setup.class-auto" : "setup.class-forced")})</dd>
          <dt>${tr("setup.power")}</dt><dd>${esc(p.heroPower)} (${p.heroPowerCost}) — ${esc(p.heroPowerText)}</dd>
          <dt>${tr("setup.deck")}</dt><dd>${tr("setup.deck-size", { source: tr(p.deckSource === "built" ? "setup.deck-built" : "setup.deck-preset"), n: p.deck.length })}</dd>
          <dt>${tr("setup.mulligan")}</dt><dd>${mull}</dd>
          <dt>${tr("setup.hand")}</dt><dd>${p.hand.map((c) => esc(c.name)).join(", ")}</dd>
        </dl>
        ${deckGroups(p.deck)}
        ${curveBars(p.deck)}
      </div>`;
    }).join("");
    return `<div class="panel" role="dialog" aria-label="${esc(tr("setup.aria"))}">
      <h2>${tr("setup.title", { seed: esc(state.seed) })}</h2>
      <p style="text-align:center;margin:0 0 10px">${tr("setup.first", { first: esc(state.first), second: esc(state.second) })}</p>
      <div class="setup-grid">${players}</div>
      <div class="panel-actions"><button class="btn" data-action="begin">${tr("setup.begin")}</button></div>
    </div>`;
  }

  function resultHtml(state) {
    const r = state.result;
    const title = r.winner === "DRAW" ? tr("result.draw") : tr("result.win", { winner: esc(r.winner) });
    return `<div class="panel" role="dialog" aria-label="${esc(tr("result.aria"))}">
      <div class="result-banner">🏆 ${title}</div>
      <p style="text-align:center">${tr("result.reason", { reason: esc(r.reason), rounds: r.rounds })}</p>
      <table class="result-table">
        <tr><th></th><th>${esc(r.player1)}</th><th>${esc(r.player2)}</th></tr>
        <tr><td>${tr("result.hp-left")}</td><td>${r.hp1}</td><td>${r.hp2}</td></tr>
        <tr><td>${tr("result.damage")}</td><td>${r.damage1}</td><td>${r.damage2}</td></tr>
      </table>
      <div class="panel-actions">
        <button class="btn" data-action="restart">${tr("result.restart")}</button>
        <button class="btn btn-ghost" data-action="close">${tr("result.close")}</button>
      </div>
    </div>`;
  }

  const CHART = { width: 900, height: 170, left: 34, right: 70, top: 12, bottom: 24 };

  function chartX(frame, frames) {
    return CHART.left + (frame / Math.max(1, frames - 1)) * (CHART.width - CHART.left - CHART.right);
  }

  function chartY(hp) {
    return CHART.top + (1 - Math.min(30, Math.max(0, hp)) / 30) * (CHART.height - CHART.top - CHART.bottom);
  }

  /** HP of both players over the match; blue = first player listed, orange = second (validated palette). */
  function hpChartHtml(curve, frames) {
    const colors = ["var(--p1)", "var(--p2)"];
    const grid = [0, 10, 20, 30].map((hp) => `<line class="grid" x1="${CHART.left}" x2="${CHART.width - CHART.right}" y1="${chartY(hp)}" y2="${chartY(hp)}"></line>
      <text x="${CHART.left - 6}" y="${chartY(hp) + 4}" text-anchor="end">${hp}</text>`).join("");
    const lines = curve.players.map((name, p) => {
      const pts = curve.points.map((pt) => `${chartX(pt.frame, frames)},${chartY(pt.hp[p])}`).join(" ");
      const last = curve.points.at(-1);
      const dots = curve.points.map((pt) => `<circle class="hp-dot" cx="${chartX(pt.frame, frames)}" cy="${chartY(pt.hp[p])}" r="3" fill="${colors[p]}"></circle>
        <circle class="hp-hit" cx="${chartX(pt.frame, frames)}" cy="${chartY(pt.hp[p])}" r="9" fill="transparent" data-tip="${esc(tr("chart.hp-tip", { name, hp: pt.hp[p] }) + (pt.round ? tr("chart.hp-round", { round: pt.round }) : ""))}"></circle>`).join("");
      return `<polyline points="${pts}" fill="none" stroke="${colors[p]}" stroke-width="2" stroke-linejoin="round"></polyline>${dots}
        <text class="hp-label" x="${chartX(last.frame, frames) + 8}" y="${chartY(last.hp[p]) + 4 + (p ? 10 : -4)}">${esc(name)} ${last.hp[p]}</text>`;
    }).join("");
    const hits = curve.bigHits.map((h) => {
      const p = curve.players.indexOf(h.target);
      const pt = curve.points.filter((q) => q.frame <= h.frame).at(-1) || curve.points[0];
      return `<text class="hp-star" x="${chartX(h.frame, frames)}" y="${chartY(pt.hp[p]) - 8}" text-anchor="middle" data-tip="${esc(tr("chart.hit-tip", { source: h.source, amount: h.amount, target: h.target }))}">✦</text>`;
    }).join("");
    return `<div class="hp-head">
        <span class="legend">${curve.players.map((n, p) => `<span><span class="swatch" style="background:${colors[p]}"></span>${esc(n)}</span>`).join("")}<span>${esc(tr("chart.big-hit"))}</span></span></div>
      <svg viewBox="0 0 ${CHART.width} ${CHART.height}" role="img" aria-label="${esc(tr("chart.aria"))}">
        ${grid}${lines}${hits}<g class="hp-cursor"><line x1="0" x2="0" y1="${CHART.top - 4}" y2="${CHART.height - CHART.bottom + 4}"></line></g>
      </svg>`;
  }

  /**
   * Creates the replay view bound to the page elements; returns the controls the app needs.
   * options.onStep(event) is called each time the replay moves exactly one event forward (used for sounds).
   */
  function create(options = {}) {
    const el = {
      root: doc.getElementById("replay"), empty: doc.getElementById("replay-empty"), board: doc.getElementById("board"),
      log: doc.getElementById("log"), overlay: doc.getElementById("overlay"), scrubber: doc.getElementById("scrubber"),
      position: doc.getElementById("position"), speed: doc.getElementById("speed"),
      play: doc.querySelector('[data-action="play"]'), fxLayer: doc.getElementById("fx-layer"),
      hpChart: doc.getElementById("hp-chart"), hpPanel: doc.getElementById("hp-panel"),
      zoom: doc.getElementById("card-zoom"),
    };
    const fx = Arena.Fx.create({ board: el.board, layer: el.fxLayer, speed: () => Number(el.speed.value || 1) });
    let curve = null;
    let resultTimer = null;
    let justArrived = false;
    let timeline = null;
    let index = 0;
    let timer = null;
    let setupShown = false;

    function load(events) {
      pause();
      timeline = Arena.Replay.buildTimeline(events);
      if (!timeline.frames.length) throw new Error(tr("replay.no-events"));
      el.scrubber.max = String(timeline.frames.length - 1);
      el.log.innerHTML = timeline.frames.map((f) => f.lines.map((line) =>
        `<li data-frame="${f.index}" tabindex="-1" class="${/^\[T\d+\] =/.test(line) ? "is-separator" : ""}">${esc(line)}</li>`).join("")).join("");
      el.empty.hidden = true;
      el.root.hidden = false;
      setupShown = false;
      curve = Arena.Visuals.hpCurve(timeline);
      el.hpChart.innerHTML = hpChartHtml(curve, timeline.frames.length);
      el.hpPanel.hidden = false;
      go(Math.max(0, (timeline.turnStarts[0] || 1) - 1));
    }

    function go(target) {
      if (!timeline) return;
      const previous = index;
      index = Math.max(0, Math.min(target, timeline.frames.length - 1));
      const forward = index === previous + 1;
      const effects = forward ? Arena.Visuals.effectsFor(timeline.frames[index].event,
        timeline.frames[previous].state, timeline.frames[index].state) : [];
      const captured = fx.capture(effects);
      if (!forward) fx.clear();
      justArrived = forward;
      render();
      justArrived = false;
      if (forward) {
        fx.play(effects, captured);
        if (options.onStep) options.onStep(timeline.frames[index].event);
      }
    }

    function render() {
      const frame = timeline.frames[index];
      const state = frame.state;
      const highlight = state.highlight;
      const [bottom, top] = state.order;
      const banner = state.phase === "setup" ? tr("banner.setup") : state.phase === "ended" ? tr("banner.ended")
        : tr("banner.turn", { round: state.round, player: esc(state.current) });
      const action = describe(frame.event);
      el.board.innerHTML = handHtml(state, top, highlight) + sideHtml(state, top, highlight) + minionsHtml(state, top, highlight)
        + `<div class="center-line"><div class="turn-banner">${banner}</div></div>`
        + minionsHtml(state, bottom, highlight) + sideHtml(state, bottom, highlight) + handHtml(state, bottom, highlight)
        + (action ? `<div class="action"><span class="who">${esc(action[0])}</span>${action[1]}</div>` : "");
      showFloater(floater(frame.event));
      el.scrubber.value = String(index);
      const turnNumber = timeline.turnStarts.filter((i) => i <= index).length;
      el.position.textContent = tr("position.text", { index: index + 1, total: timeline.frames.length })
        + (state.round ? tr("position.round", { round: state.round }) : "")
        + (turnNumber ? tr("position.turn", { turn: turnNumber, turns: timeline.turnStarts.length }) : "");
      updateLog();
      updateOverlay(state);
      updateChartCursor();
      if (options.onFrame) options.onFrame(state);
    }

    function updateChartCursor() {
      const cursor = el.hpChart.querySelector(".hp-cursor");
      if (cursor) cursor.setAttribute("transform", `translate(${chartX(index, timeline.frames.length)},0)`);
    }

    function showFloater(f) {
      if (!f) return;
      const hero = el.board.querySelector(`.hero[data-hero="${CSS.escape(f.target)}"]`);
      if (!hero) return;
      const box = hero.getBoundingClientRect();
      const origin = el.board.getBoundingClientRect();
      const node = doc.createElement("div");
      node.className = "float is-" + f.kind;
      node.textContent = f.text;
      node.style.left = box.left - origin.left + box.width / 2 - 20 + "px";
      node.style.top = box.top - origin.top + 10 + "px";
      el.board.appendChild(node);
    }

    function updateLog() {
      let current = null;
      el.log.querySelectorAll("li").forEach((li) => {
        const frame = Number(li.dataset.frame);
        li.classList.toggle("is-current", frame === index);
        li.classList.toggle("is-past", frame < index);
        li.classList.toggle("is-future", frame > index);
        if (frame === index && !current) current = li;
      });
      if (current) {
        const top = current.offsetTop - el.log.offsetTop;
        if (top < el.log.scrollTop || top > el.log.scrollTop + el.log.clientHeight - 40) {
          el.log.scrollTop = top - el.log.clientHeight / 3;
        }
      }
    }

    function updateOverlay(state) {
      if (state.phase === "setup" && !setupShown) {
        el.overlay.innerHTML = setupHtml(state);
        el.overlay.hidden = false;
      } else if (state.phase === "ended" && index === timeline.frames.length - 1 && !timer) {
        clearTimeout(resultTimer);
        const show = () => {
          if (index !== timeline.frames.length - 1 || timer) return;
          el.overlay.innerHTML = resultHtml(state);
          el.overlay.hidden = false;
        };
        if (justArrived) {
          el.overlay.hidden = true;
          resultTimer = setTimeout(show, 2200);
        } else {
          show();
        }
      } else {
        el.overlay.hidden = true;
      }
    }

    function delay() {
      const next = timeline.frames[index + 1];
      const factor = next && next.event.type === "TurnStarted" ? 1.6 : 1;
      return (BASE_DELAY_MS * factor) / Number(el.speed.value || 1);
    }

    function tick() {
      if (index >= timeline.frames.length - 1) {
        pause();
        render();
        return;
      }
      go(index + 1);
      timer = setTimeout(tick, delay());
    }

    function play() {
      if (!timeline || timer) return;
      setupShown = true;
      if (index >= timeline.frames.length - 1) go(0);
      el.play.textContent = "⏸";
      el.play.setAttribute("aria-label", tr("ctl.pause.label"));
      timer = setTimeout(tick, 150);
      el.overlay.hidden = true;
    }

    function pause() {
      if (timer) clearTimeout(timer);
      timer = null;
      el.play.textContent = "▶";
      el.play.setAttribute("aria-label", tr("ctl.play.label"));
    }

    function nextTurn(direction) {
      const starts = timeline.turnStarts;
      const target = direction > 0 ? starts.find((i) => i > index) : [...starts].reverse().find((i) => i < index);
      go(target === undefined ? (direction > 0 ? timeline.frames.length - 1 : 0) : target);
    }

    function action(name) {
      if (!timeline) return;
      if (name !== "play") pause();
      if (name !== "play" && name !== "begin") setupShown = true;
      switch (name) {
        case "play": return timer ? (pause(), render()) : play();
        case "begin": setupShown = true; return play();
        case "next": return go(index + 1);
        case "prev": return go(index - 1);
        case "next-turn": return nextTurn(1);
        case "prev-turn": return nextTurn(-1);
        case "start": return go(0);
        case "end": return go(timeline.frames.length - 1);
        case "restart": setupShown = false; return go(Math.max(0, (timeline.turnStarts[0] || 1) - 1));
        case "close": el.overlay.hidden = true; return undefined;
        default: return undefined;
      }
    }

    doc.getElementById("controls").addEventListener("click", (e) => {
      const button = e.target.closest("[data-action]");
      if (button) action(button.dataset.action);
    });
    el.overlay.addEventListener("click", (e) => {
      const button = e.target.closest("[data-action]");
      if (button) action(button.dataset.action);
    });
    el.scrubber.addEventListener("input", () => {
      pause();
      setupShown = true;
      go(Number(el.scrubber.value));
    });
    el.hpChart.addEventListener("click", (e) => {
      const svg = el.hpChart.querySelector("svg");
      if (!svg || !timeline) return;
      const box = svg.getBoundingClientRect();
      const x = ((e.clientX - box.left) / box.width) * CHART.width;
      const frame = Math.round(((x - CHART.left) / (CHART.width - CHART.left - CHART.right)) * (timeline.frames.length - 1));
      pause();
      setupShown = true;
      go(frame);
    });
    try {
      el.hpPanel.open = root.localStorage.getItem("arena.hpOpen") === "true";
    } catch (e) {
      /* storage unavailable: the section starts closed */
    }
    el.hpPanel.addEventListener("toggle", () => {
      try {
        root.localStorage.setItem("arena.hpOpen", String(el.hpPanel.open));
      } catch (e) {
        /* storage unavailable: the choice lasts for this visit only */
      }
    });
    const tooltip = doc.getElementById("tooltip");
    el.hpChart.addEventListener("mousemove", (e) => {
      const mark = e.target.closest("[data-tip]");
      if (!mark) {
        tooltip.hidden = true;
        return;
      }
      tooltip.textContent = mark.getAttribute("data-tip");
      tooltip.hidden = false;
      tooltip.style.left = Math.min(e.clientX + 14, root.innerWidth - tooltip.offsetWidth - 8) + "px";
      tooltip.style.top = e.clientY + 14 + "px";
    });
    el.hpChart.addEventListener("mouseleave", () => (tooltip.hidden = true));
    el.board.addEventListener("mouseover", (e) => showZoom(e.target.closest(".card")));
    el.board.addEventListener("focusin", (e) => showZoom(e.target.closest(".card")));
    el.board.addEventListener("mouseout", (e) => { if (e.target.closest(".card")) el.zoom.hidden = true; });
    el.board.addEventListener("focusout", () => (el.zoom.hidden = true));

    function showZoom(cardEl) {
      if (!cardEl) return;
      const card = { name: cardEl.dataset.name, cost: cardEl.dataset.cost, category: cardEl.dataset.cat,
        text: cardEl.dataset.text, icon: art("card", cardEl.dataset.name, cardEl.dataset.cat) };
      el.zoom.innerHTML = Arena.Fx.bigCardHtml(card);
      el.zoom.hidden = false;
      const box = cardEl.getBoundingClientRect();
      const top = box.top > root.innerHeight / 2 ? box.top - el.zoom.offsetHeight - 10 : box.bottom + 10;
      el.zoom.style.left = Math.max(8, Math.min(box.left + box.width / 2 - el.zoom.offsetWidth / 2, root.innerWidth - el.zoom.offsetWidth - 8)) + "px";
      el.zoom.style.top = Math.max(8, top) + "px";
    }

    el.log.addEventListener("click", (e) => {
      const li = e.target.closest("li[data-frame]");
      if (li) {
        pause();
        setupShown = true;
        go(Number(li.dataset.frame));
      }
    });

    /** A new design theme changes the pictures of the frame on screen, not only the next ones. */
    function redraw() {
      if (timeline) render();
    }

    /** A new language rewrites the texts of the frame on screen, the HP chart and the play button. */
    function relabel() {
      el.play.setAttribute("aria-label", tr(timer ? "ctl.pause.label" : "ctl.play.label"));
      if (!timeline) return;
      el.hpChart.innerHTML = hpChartHtml(curve, timeline.frames.length);
      render();
    }

    el.play.setAttribute("aria-label", tr("ctl.play.label"));

    return { load, action, redraw, relabel, isLoaded: () => !!timeline, speed: () => Number(el.speed.value || 1) };
  }

  Arena.ReplayView = { create };
})(window);
