/*
 * One plain-language message per event ("who" + what happened), shown on the board so a viewer
 * understands the match without reading the technical log. Pure function (texts from i18n.js),
 * tested in tests.html.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});

  const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const t = (key, params) => Arena.I18n.t(key, params);
  const count = (key, n) => t(key + (n > 1 ? ".many" : ".one"), { n });

  /** [who, html] for an event; {@code before} is the state before it (to tell whose turn just ended). */
  function describe(event, before) {
    const e = event;
    switch (e.type) {
      case "MatchStarted": return [t("narr.arena"), t("narr.match-started", { p1: esc(e.player1), p2: esc(e.player2) })];
      case "PlayerSetUp": return [e.player, t("narr.setup", { cls: esc(e.heroClass), power: esc(e.heroPower) })];
      case "FirstPlayerChosen": return [t("narr.coin"), t("narr.first", { first: esc(e.first), second: esc(e.second) })];
      case "MulliganDone": return [e.player, e.putBack.length
        ? t("narr.mulligan-swap", { cards: count("narr.cards", e.putBack.length) }) : t("narr.mulligan-keep")];
      case "OpeningHand": return [e.player, t("narr.opening", { cards: count("narr.cards", e.cards.length) })];
      case "TurnStarted": {
        const ended = before && before.current && before.current !== e.player;
        return [ended ? t("narr.turn-end", { player: before.current }) : t("narr.turn-start"),
          t("narr.turn", { player: esc(e.player), round: e.round, hp: e.hp })];
      }
      case "CardDrawn": return [e.player, t("narr.draw", { card: esc(e.card) })];
      case "ManaRefilled": return [e.player, t("narr.mana", { mana: e.mana }) + (e.frozen ? t("narr.frozen") : "")];
      case "CardPlayed": return [e.player, t("action.card-played", { card: esc(e.card), cost: e.cost })];
      case "HeroPowerUsed": return [e.player, t("action.hero-power", { power: esc(e.power) })];
      case "DamageDealt": return [e.source, t("action.damage", { amount: e.hpBefore - e.hpAfter, target: esc(e.target) })
        + (e.absorbed ? t("action.absorbed", { n: e.absorbed }) : "")];
      case "Healed": return [e.source, t("action.heal", { player: esc(e.player), amount: e.amount })];
      case "ArmorGained": return [e.source, t("action.armor", { amount: e.amount, player: esc(e.player) })];
      case "ArmorExpired": return [e.player, t("narr.armor-expired", { amount: e.amount })];
      case "ManaGained": return [e.player, t("narr.mana-gained", { source: esc(e.source), mana: e.mana, max: e.maxMana })];
      case "MinionSummoned": return [e.owner, t("action.summon", { minion: esc(e.minion), attack: e.attack, health: e.health })
        + (e.taunt ? t("action.summon-taunt") : "")];
      case "SummonFizzled": return [e.owner, t("narr.fizzle", { minion: esc(e.minion) })];
      case "MinionAttacked": return [e.owner, t("action.minion-attack", { minion: esc(e.minion), target: esc(e.target) })];
      case "MinionDamaged": return [e.source, t("narr.minion-damaged", { minion: esc(e.minion), amount: e.amount })];
      case "MinionDied": return [e.owner, t("action.minion-died", { minion: esc(e.minion) })];
      case "StatusApplied": return [e.source, t("action.status", { target: esc(e.target), status: esc(e.status) })];
      case "EvasionTriggered": return [e.player, t("action.evasion", { amount: e.prevented, source: esc(e.source) })];
      case "PoisonTicked": return [e.player, t("action.poison", { amount: e.amount })];
      case "FatigueDamage": return [e.player, t("action.fatigue", { amount: e.amount })];
      case "CardBurned": return [e.player, t("action.burn", { card: esc(e.card) })];
      case "IllegalAction": return [e.player, t("action.illegal", { reason: esc(e.reason) })];
      case "BotSpoke": return e.message ? [e.player, t("narr.says", { message: esc(e.message) })]
        : [e.player, t("narr.thinks", { thought: esc(e.thought) })];
      case "MatchEnded": return [t("narr.end"), e.winner === "DRAW" ? t("narr.draw-result")
        : t("narr.winner", { winner: esc(e.winner), reason: esc(e.reason), rounds: count("narr.rounds", e.rounds) })];
      default: return null;
    }
  }

  Arena.Narration = { describe };
})(typeof window !== "undefined" ? window : globalThis);
