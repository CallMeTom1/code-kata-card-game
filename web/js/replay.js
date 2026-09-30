/*
 * Replay model: turns the events exported by the Java engine (--json) into board states.
 * Pure functions only (no DOM), so the whole logic is covered by tests.html.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const COIN = { name: "The Coin", cost: 0, category: "RESOURCE", text: "Gain 1 mana this turn only." };

  /** One JSON object per line; blank lines are ignored; errors name the line. */
  function parseJsonl(text) {
    const events = [];
    String(text).split(/\r?\n/).forEach((line, i) => {
      if (!line.trim()) return;
      let event;
      try {
        event = JSON.parse(line);
      } catch (e) {
        throw new Error("Invalid JSON on line " + (i + 1) + ": " + e.message);
      }
      if (!event || typeof event.type !== "string") {
        throw new Error("Missing event type on line " + (i + 1));
      }
      events.push(event);
    });
    return events;
  }

  function initialState() {
    return { phase: "setup", round: 0, current: null, order: [], players: {}, first: null, second: null,
      seed: null, result: null, highlight: null, nextMinionId: 1 };
  }

  function newPlayer(name, label) {
    return { name, label, bot: "", heroClass: "", classChoice: "", deckSource: "", heroPower: "", heroPowerCost: 2,
      heroPowerText: "", deck: [], catalog: {}, mulligan: null, hp: 30, maxHp: 30, armor: 0, mana: 0, maxMana: 0,
      hand: [], deckCount: 0, board: [], frozen: false, frozenThisTurn: false, poisons: [], evasion: false,
      evasionTurns: 0, parry: 0, attackBuff: 0, fatigue: 0 };
  }

  function card(player, name) {
    if (name === COIN.name) return { ...COIN };
    const known = player.catalog[name];
    return known ? { name, cost: known.cost, category: known.category, text: known.text }
      : { name, cost: 0, category: "UTILITY", text: "" };
  }

  function findMinion(player, name, prefer) {
    const candidates = player.board.filter((m) => m.name === name);
    return candidates.find(prefer) || candidates[0];
  }

  /** Returns a new state; the given state is never modified, so every frame of the timeline stays valid. */
  function apply(previous, event) {
    const s = structuredClone(previous);
    s.highlight = null;
    const p = (name) => s.players[name];
    switch (event.type) {
      case "MatchStarted":
        s.order = [event.player1, event.player2];
        s.players[event.player1] = newPlayer(event.player1, event.player1Label);
        s.players[event.player2] = newPlayer(event.player2, event.player2Label);
        s.seed = event.seed;
        break;
      case "PlayerSetUp": {
        const pl = p(event.player);
        Object.assign(pl, { bot: event.bot, heroClass: event.heroClass, classChoice: event.classChoice,
          deckSource: event.deckSource, heroPower: event.heroPower, heroPowerCost: event.heroPowerCost,
          heroPowerText: event.heroPowerText, deck: event.deck, deckCount: event.deck.length });
        event.deck.forEach((c) => (pl.catalog[c.name] = { cost: c.cost, category: c.category, text: c.text || "" }));
        break;
      }
      case "FirstPlayerChosen":
        s.first = event.first;
        s.second = event.second;
        break;
      case "MulliganDone":
        p(event.player).mulligan = { putBack: event.putBack, drawn: event.drawn };
        break;
      case "OpeningHand": {
        const pl = p(event.player);
        pl.hand = event.cards.map((name) => card(pl, name));
        pl.deckCount = pl.deck.length - pl.hand.filter((c) => c.name !== COIN.name).length;
        break;
      }
      case "TurnStarted": {
        const pl = p(event.player);
        s.phase = "play";
        s.round = event.round;
        s.current = event.player;
        Object.assign(pl, { hp: event.hp, armor: event.armor, deckCount: event.deckSize, parry: 0 });
        if (pl.evasionTurns > 0 && --pl.evasionTurns === 0) pl.evasion = false;
        s.highlight = { kind: "turn", player: event.player };
        break;
      }
      case "ManaRefilled":
        Object.assign(p(event.player), { mana: event.mana, maxMana: event.maxMana, frozen: false,
          frozenThisTurn: event.frozen });
        break;
      case "CardDrawn":
        p(event.player).hand.push(card(p(event.player), event.card));
        p(event.player).deckCount--;
        s.highlight = { kind: "draw", player: event.player, card: event.card };
        break;
      case "CardBurned":
        p(event.player).deckCount--;
        s.highlight = { kind: "burn", player: event.player, card: event.card };
        break;
      case "FatigueDamage":
        Object.assign(p(event.player), { hp: event.hpAfter, fatigue: event.amount });
        s.highlight = { kind: "damage", target: event.player, amount: event.amount, source: "Fatigue" };
        break;
      case "ArmorExpired":
        p(event.player).armor = event.armorLeft;
        break;
      case "PoisonTicked": {
        const pl = p(event.player);
        pl.hp = event.hpAfter;
        pl.poisons = pl.poisons.map((x) => ({ ...x, turns: x.turns - 1 })).filter((x) => x.turns > 0);
        s.highlight = { kind: "damage", target: event.player, amount: event.amount, source: "Poison" };
        break;
      }
      case "CardPlayed": {
        const pl = p(event.player);
        const index = pl.hand.findIndex((c) => c.name === event.card);
        const played = index >= 0 ? pl.hand.splice(index, 1)[0] : card(pl, event.card);
        pl.mana = event.manaLeft;
        if (played.category === "ATTACK") pl.attackBuff = 0;
        s.highlight = { kind: "play", player: event.player, card: event.card };
        break;
      }
      case "HeroPowerUsed":
        p(event.player).mana = event.manaLeft;
        s.highlight = { kind: "power", player: event.player, card: event.power };
        break;
      case "IllegalAction":
        s.highlight = { kind: "illegal", player: event.player, reason: event.reason };
        break;
      case "DamageDealt":
        Object.assign(p(event.target), { hp: event.hpAfter, armor: event.armorAfter });
        s.highlight = { kind: "damage", target: event.target, amount: event.hpBefore - event.hpAfter,
          source: event.source };
        break;
      case "EvasionTriggered":
        Object.assign(p(event.player), { evasion: false, evasionTurns: 0 });
        s.highlight = { kind: "evade", target: event.player, source: event.source };
        break;
      case "Healed":
        p(event.player).hp = event.hpAfter;
        s.highlight = { kind: "heal", target: event.player, amount: event.amount, source: event.source };
        break;
      case "ArmorGained":
        p(event.player).armor = event.totalArmor;
        s.highlight = { kind: "armor", target: event.player, amount: event.amount, source: event.source };
        break;
      case "ManaGained":
        Object.assign(p(event.player), { mana: event.mana, maxMana: event.maxMana });
        break;
      case "StatusApplied":
        applyStatus(p(event.target), event.status);
        s.highlight = { kind: "status", target: event.target, status: event.status };
        break;
      case "MinionSummoned":
        p(event.owner).board.push({ id: s.nextMinionId++, name: event.minion, attack: event.attack,
          health: event.health, maxHealth: event.health, taunt: event.taunt });
        s.highlight = { kind: "summon", player: event.owner, minionId: s.nextMinionId - 1 };
        break;
      case "SummonFizzled":
        s.highlight = { kind: "illegal", player: event.owner, reason: event.minion + " fizzles (board full)" };
        break;
      case "MinionAttacked": {
        const attacker = findMinion(p(event.owner), event.minion, (m) => m.health === event.health);
        s.highlight = { kind: "minion-attack", player: event.owner, minionId: attacker && attacker.id,
          target: event.target };
        break;
      }
      case "MinionDamaged": {
        const minion = findMinion(p(event.owner), event.minion, (m) => m.health === event.healthLeft + event.amount);
        if (minion) minion.health = event.healthLeft;
        s.highlight = { kind: "minion-damage", player: event.owner, minionId: minion && minion.id };
        break;
      }
      case "MinionDied": {
        const pl = p(event.owner);
        const dead = findMinion(pl, event.minion, (m) => m.health <= 0);
        pl.board = pl.board.filter((m) => m !== dead);
        break;
      }
      case "MatchEnded":
        s.phase = "ended";
        s.result = event;
        break;
      default:
        break;
    }
    return s;
  }

  function applyStatus(player, status) {
    let m;
    if (status.startsWith("FROZEN")) player.frozen = true;
    else if ((m = status.match(/^POISON (\d+) \((\d+) turns?\)/))) player.poisons.push({ amount: +m[1], turns: +m[2] });
    else if (status.startsWith("EVASION")) Object.assign(player, { evasion: true, evasionTurns: 2 });
    else if ((m = status.match(/^PARRY (\d+)/))) player.parry += +m[1];
    else if ((m = status.match(/^NEXT ATTACK \+(\d+)/))) player.attackBuff = +m[1];
  }

  /** One frame per event (state after it, plus its log lines), and where each turn starts. */
  function buildTimeline(events) {
    const format = Arena.LogFormat ? Arena.LogFormat.create() : () => [];
    const frames = [];
    const turnStarts = [];
    let state = initialState();
    events.forEach((event, index) => {
      state = apply(state, event);
      frames.push({ index, event, state, lines: format(event) });
      if (event.type === "TurnStarted") turnStarts.push(index);
    });
    return { frames, turnStarts };
  }

  Arena.Replay = { parseJsonl, initialState, apply, buildTimeline };
})(typeof window !== "undefined" ? window : globalThis);
