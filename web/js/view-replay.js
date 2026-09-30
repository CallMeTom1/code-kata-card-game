/*
 * Replay view: draws one frame of the timeline (board, hands, heroes, minions, log) and drives playback.
 * All game logic lives in replay.js; this file only reads frames and writes DOM.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const doc = root.document;

  const CLASS_ICONS = { Mage: "🔮", Tank: "🛡️", Swordsman: "⚔️", Assassin: "🗡️", Cleric: "✨" };
  const CATEGORY_SHORT = { ATTACK: "ATQ", DEFENSE: "DEF", RESOURCE: "RES", UTILITY: "UTI" };
  const CATEGORY_NAMES = { ATTACK: "Attaque", DEFENSE: "Défense", RESOURCE: "Ressource", UTILITY: "Utilitaire" };
  const BASE_DELAY_MS = 900;

  const esc = (text) => String(text).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;" }[c]));

  function cardHtml(card, isNew) {
    return `<div class="card${isNew ? " is-new" : ""}" data-cat="${esc(card.category)}" title="${esc(card.name)} — ${esc(CATEGORY_NAMES[card.category] || card.category)}, coût ${card.cost}">
      <div class="cost"><span>${card.cost}</span></div>
      <div class="card-name">${esc(card.name)}</div>
      <div class="card-cat">${CATEGORY_SHORT[card.category] || "?"}</div>
    </div>`;
  }

  function badges(p) {
    const list = [];
    if (p.frozen) list.push("❄ Gelé");
    if (p.frozenThisTurn) list.push("❄ −1 mana ce tour");
    p.poisons.forEach((x) => list.push(`☠ Poison ${x.amount} (${x.turns})`));
    if (p.evasion) list.push("💨 Evasion");
    if (p.parry) list.push(`🤺 Parade ${p.parry}`);
    if (p.attackBuff) list.push(`⚔ Attaque +${p.attackBuff}`);
    if (p.fatigue) list.push(`😵 Fatigue ${p.fatigue}`);
    return list.map((b) => `<span class="badge">${esc(b)}</span>`).join("");
  }

  function manaHtml(p) {
    let gems = "";
    for (let i = 0; i < 10; i++) {
      const cls = i < p.mana ? "is-full" : i < p.maxMana ? "is-spent" : "";
      gems += `<span class="crystal ${cls}"></span>`;
    }
    return `<div class="mana" title="Mana ${p.mana}/${p.maxMana}">${gems}<span>&nbsp;${p.mana}/${p.maxMana}</span></div>`;
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
        <div class="deck-pile" title="Cartes restantes dans le deck">🂠 Deck ${p.deckCount} · Main ${p.hand.length}</div>
        <div class="badges">${badges(p)}</div>
      </div>
      <div class="hero-block">
        <div class="hero${current ? " is-current" : ""}${dead ? " is-dead" : ""}" data-class="${esc(p.heroClass)}" data-hero="${esc(name)}" title="${esc(name)} : ${p.hp} PV, ${p.armor} armure">
          <span aria-hidden="true">${CLASS_ICONS[p.heroClass] || "🎴"}</span>
          ${p.armor > 0 ? `<div class="gem gem-armor" title="Armure">${p.armor}</div>` : ""}
          <div class="gem gem-hp" title="Points de vie">${p.hp}</div>
        </div>
        <div class="power${powerUsed ? " is-used" : ""}" title="Pouvoir héroïque : ${esc(p.heroPower)} — ${esc(p.heroPowerText)}">
          <div class="cost"><span>${p.heroPowerCost}</span></div>${esc(p.heroPower)}
        </div>
      </div>
      <div class="side-info right">${manaHtml(p)}</div>
    </div>`;
  }

  function handHtml(state, name, highlight) {
    const p = state.players[name];
    const lastDrawn = highlight && highlight.kind === "draw" && highlight.player === name ? p.hand.length - 1 : -1;
    return `<div class="hand" aria-label="Main de ${esc(name)}">${p.hand.map((c, i) => cardHtml(c, i === lastDrawn)).join("")}</div>`;
  }

  function minionsHtml(state, name, highlight) {
    const p = state.players[name];
    const acting = highlight && highlight.kind === "minion-attack" ? highlight.minionId : null;
    const hit = highlight && highlight.kind === "minion-damage" ? highlight.minionId : null;
    return `<div class="minions" aria-label="Plateau de ${esc(name)}">${p.board.map((m) => `
      <div class="minion${m.taunt ? " is-taunt" : ""}${m.id === acting ? " is-acting" : ""}${m.id === hit ? " is-hit" : ""}" title="${esc(m.name)} ${m.attack}/${m.health}${m.taunt ? " — Taunt" : ""}">
        ${esc(m.name)}
        <div class="stat atk">${m.attack}</div>
        <div class="stat hp${m.health < m.maxHealth ? " is-hurt" : ""}">${m.health}</div>
      </div>`).join("")}</div>`;
  }

  function describe(event) {
    switch (event.type) {
      case "CardPlayed": return [event.player, `joue <b>${esc(event.card)}</b> (${event.cost} mana)`];
      case "HeroPowerUsed": return [event.player, `utilise son pouvoir <b>${esc(event.power)}</b>`];
      case "DamageDealt": return [event.source, `inflige <b>${event.hpBefore - event.hpAfter}</b> à ${esc(event.target)}`
        + (event.absorbed ? ` (${event.absorbed} absorbés)` : "")];
      case "Healed": return [event.source, `soigne ${esc(event.player)} de <b>${event.amount}</b>`];
      case "ArmorGained": return [event.source, `donne <b>${event.amount}</b> d'armure à ${esc(event.player)}`];
      case "MinionSummoned": return [event.owner, `invoque <b>${esc(event.minion)}</b> ${event.attack}/${event.health}${event.taunt ? " (Taunt)" : ""}`];
      case "MinionAttacked": return [event.owner, `${esc(event.minion)} attaque <b>${esc(event.target)}</b>`];
      case "MinionDied": return [event.owner, `${esc(event.minion)} est détruit`];
      case "StatusApplied": return [event.source, `${esc(event.target)} : <b>${esc(event.status)}</b>`];
      case "EvasionTriggered": return [event.player, `esquive ${event.prevented} dégâts (${esc(event.source)})`];
      case "PoisonTicked": return [event.player, `subit <b>${event.amount}</b> de poison`];
      case "FatigueDamage": return [event.player, `n'a plus de cartes : fatigue <b>${event.amount}</b>`];
      case "CardBurned": return [event.player, `main pleine, <b>${esc(event.card)}</b> est détruite`];
      case "IllegalAction": return [event.player, `action refusée : ${esc(event.reason)}`];
      case "ManaGained": return [event.source, `mana ${event.mana}/${event.maxMana}`];
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
    return `<div class="curve" aria-label="Courbe de mana">${buckets.map((n, cost) =>
      `<div style="height:${Math.round((n / max) * 100)}%" title="${n} carte(s) à ${cost === 5 ? "5+" : cost} mana"><em>${n}</em><span>${cost === 5 ? "5+" : cost}</span></div>`).join("")}</div>`;
  }

  function deckGroups(deck) {
    return Object.keys(CATEGORY_NAMES).map((cat) => {
      const counts = new Map();
      deck.filter((c) => c.category === cat).forEach((c) => counts.set(c.name, (counts.get(c.name) || 0) + 1));
      if (!counts.size) return "";
      const total = [...counts.values()].reduce((a, b) => a + b, 0);
      return `<div class="deck-group" data-cat="${cat}"><b>${CATEGORY_SHORT[cat]} ${total}</b>${[...counts].map(([n, k]) => `${esc(n)} ×${k}`).join(", ")}</div>`;
    }).join("");
  }

  function setupHtml(state) {
    const players = state.order.map((name, i) => {
      const p = state.players[name];
      const mull = p.mulligan && p.mulligan.putBack.length
        ? `remet ${p.mulligan.putBack.map(esc).join(", ")} → pioche ${p.mulligan.drawn.map(esc).join(", ")}` : "garde toute sa main";
      return `<div class="setup-player">
        <h3><span class="swatch" style="background:var(--p${i + 1})"></span>${CLASS_ICONS[p.heroClass] || ""} ${esc(name)}</h3>
        <dl>
          <dt>Bot</dt><dd>${esc(p.bot)}</dd>
          <dt>Classe</dt><dd>${esc(p.heroClass)} (${p.classChoice === "auto" ? "choisie par le bot" : "imposée"})</dd>
          <dt>Pouvoir</dt><dd>${esc(p.heroPower)} (${p.heroPowerCost}) — ${esc(p.heroPowerText)}</dd>
          <dt>Deck</dt><dd>${p.deckSource === "built" ? "construit par le bot" : "deck prédéfini"}, ${p.deck.length} cartes</dd>
          <dt>Mulligan</dt><dd>${mull}</dd>
          <dt>Main</dt><dd>${p.hand.map((c) => esc(c.name)).join(", ")}</dd>
        </dl>
        ${deckGroups(p.deck)}
        ${curveBars(p.deck)}
      </div>`;
    }).join("");
    return `<div class="panel" role="dialog" aria-label="Préparation du match">
      <h2>Préparation du match · graine ${esc(state.seed)}</h2>
      <p style="text-align:center;margin:0 0 10px">🪙 <b>${esc(state.first)}</b> commence (pile ou face) — <b>${esc(state.second)}</b> reçoit The Coin</p>
      <div class="setup-grid">${players}</div>
      <div class="panel-actions"><button class="btn" data-action="begin">Commencer ▶</button></div>
    </div>`;
  }

  function resultHtml(state) {
    const r = state.result;
    const title = r.winner === "DRAW" ? "Match nul" : `Victoire de ${esc(r.winner)} !`;
    return `<div class="panel" role="dialog" aria-label="Résultat">
      <div class="result-banner">🏆 ${title}</div>
      <p style="text-align:center">Raison : <b>${esc(r.reason)}</b> · ${r.rounds} tours</p>
      <table class="result-table">
        <tr><th></th><th>${esc(r.player1)}</th><th>${esc(r.player2)}</th></tr>
        <tr><td>PV restants</td><td>${r.hp1}</td><td>${r.hp2}</td></tr>
        <tr><td>Dégâts infligés</td><td>${r.damage1}</td><td>${r.damage2}</td></tr>
      </table>
      <div class="panel-actions">
        <button class="btn" data-action="restart">↺ Revoir</button>
        <button class="btn btn-ghost" data-action="close">Fermer</button>
      </div>
    </div>`;
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
      play: doc.querySelector('[data-action="play"]'),
    };
    let timeline = null;
    let index = 0;
    let timer = null;
    let setupShown = false;

    function load(events) {
      pause();
      timeline = Arena.Replay.buildTimeline(events);
      if (!timeline.frames.length) throw new Error("Le fichier ne contient aucun événement.");
      el.scrubber.max = String(timeline.frames.length - 1);
      el.log.innerHTML = timeline.frames.map((f) => f.lines.map((line) =>
        `<li data-frame="${f.index}" tabindex="-1" class="${/^\[T\d+\] =/.test(line) ? "is-separator" : ""}">${esc(line)}</li>`).join("")).join("");
      el.empty.hidden = true;
      el.root.hidden = false;
      setupShown = false;
      go(Math.max(0, (timeline.turnStarts[0] || 1) - 1));
    }

    function go(target) {
      if (!timeline) return;
      const previous = index;
      index = Math.max(0, Math.min(target, timeline.frames.length - 1));
      render();
      if (index === previous + 1 && options.onStep) options.onStep(timeline.frames[index].event);
    }

    function render() {
      const frame = timeline.frames[index];
      const state = frame.state;
      const highlight = state.highlight;
      const [bottom, top] = state.order;
      const banner = state.phase === "setup" ? "Préparation" : state.phase === "ended" ? "Fin du match"
        : `Tour ${state.round} · ${esc(state.current)}`;
      const action = describe(frame.event);
      el.board.innerHTML = handHtml(state, top, highlight) + sideHtml(state, top, highlight) + minionsHtml(state, top, highlight)
        + `<div class="center-line"><div class="turn-banner">${banner}</div></div>`
        + minionsHtml(state, bottom, highlight) + sideHtml(state, bottom, highlight) + handHtml(state, bottom, highlight)
        + (action ? `<div class="action"><span class="who">${esc(action[0])}</span>${action[1]}</div>` : "");
      showFloater(floater(frame.event));
      el.scrubber.value = String(index);
      const turnNumber = timeline.turnStarts.filter((i) => i <= index).length;
      el.position.textContent = `Événement ${index + 1} / ${timeline.frames.length}` + (state.round ? ` · manche ${state.round}` : "")
        + (turnNumber ? ` · tour de jeu ${turnNumber}/${timeline.turnStarts.length}` : "");
      updateLog();
      updateOverlay(state);
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
        el.overlay.innerHTML = resultHtml(state);
        el.overlay.hidden = false;
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
      el.play.setAttribute("aria-label", "Pause");
      timer = setTimeout(tick, 150);
      el.overlay.hidden = true;
    }

    function pause() {
      if (timer) clearTimeout(timer);
      timer = null;
      el.play.textContent = "▶";
      el.play.setAttribute("aria-label", "Lecture");
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
    el.log.addEventListener("click", (e) => {
      const li = e.target.closest("li[data-frame]");
      if (li) {
        pause();
        setupShown = true;
        go(Number(li.dataset.frame));
      }
    });

    return { load, action, isLoaded: () => !!timeline, speed: () => Number(el.speed.value || 1) };
  }

  Arena.ReplayView = { create };
})(window);
