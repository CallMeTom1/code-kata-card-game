/*
 * One plain-language message per event ("who" + what happened), shown on the board so a viewer
 * understands the match without reading the technical log. Pure function, tested in tests.html.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});

  const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const plural = (n, word) => n + " " + word + (n > 1 ? "s" : "");

  /** [who, html] for an event; {@code before} is the state before it (to tell whose turn just ended). */
  function describe(event, before) {
    const e = event;
    switch (e.type) {
      case "MatchStarted": return ["Arène", `Le combat commence : <b>${esc(e.player1)}</b> contre <b>${esc(e.player2)}</b>`];
      case "PlayerSetUp": return [e.player, `choisit la classe <b>${esc(e.heroClass)}</b> (pouvoir ${esc(e.heroPower)})`];
      case "FirstPlayerChosen": return ["Pile ou face", `<b>${esc(e.first)}</b> commence, ${esc(e.second)} reçoit The Coin`];
      case "MulliganDone": return [e.player, e.putBack.length ? `échange ${plural(e.putBack.length, "carte")} de sa main`
        : "garde toute sa main"];
      case "OpeningHand": return [e.player, `commence avec ${plural(e.cards.length, "carte")} en main`];
      case "TurnStarted": {
        const who = before && before.current && before.current !== e.player ? `Fin du tour de ${before.current}` : "Début du combat";
        return [who, `À <b>${esc(e.player)}</b> de jouer · manche ${e.round} · ${e.hp} PV`];
      }
      case "CardDrawn": return [e.player, `pioche <b>${esc(e.card)}</b>`];
      case "ManaRefilled": return [e.player, `dispose de <b>${e.mana}</b> mana${e.frozen ? " (gelé : −1)" : ""}`];
      case "CardPlayed": return [e.player, `joue <b>${esc(e.card)}</b> (${e.cost} mana)`];
      case "HeroPowerUsed": return [e.player, `utilise son pouvoir <b>${esc(e.power)}</b>`];
      case "DamageDealt": return [e.source, `inflige <b>${e.hpBefore - e.hpAfter}</b> à ${esc(e.target)}`
        + (e.absorbed ? ` (${e.absorbed} absorbés)` : "")];
      case "Healed": return [e.source, `soigne ${esc(e.player)} de <b>${e.amount}</b>`];
      case "ArmorGained": return [e.source, `donne <b>${e.amount}</b> d'armure à ${esc(e.player)}`];
      case "ArmorExpired": return [e.player, `perd ${e.amount} d'armure (expirée)`];
      case "ManaGained": return [e.player, `${esc(e.source)} : mana <b>${e.mana}</b> (max ${e.maxMana})`];
      case "MinionSummoned": return [e.owner, `invoque <b>${esc(e.minion)}</b> ${e.attack}/${e.health}${e.taunt ? " (Taunt)" : ""}`];
      case "SummonFizzled": return [e.owner, `plateau plein : ${esc(e.minion)} n'arrive pas`];
      case "MinionAttacked": return [e.owner, `${esc(e.minion)} attaque <b>${esc(e.target)}</b>`];
      case "MinionDamaged": return [e.source, `blesse ${esc(e.minion)} de <b>${e.amount}</b>`];
      case "MinionDied": return [e.owner, `${esc(e.minion)} est détruit`];
      case "StatusApplied": return [e.source, `${esc(e.target)} : <b>${esc(e.status)}</b>`];
      case "EvasionTriggered": return [e.player, `esquive ${e.prevented} dégâts (${esc(e.source)})`];
      case "PoisonTicked": return [e.player, `subit <b>${e.amount}</b> de poison`];
      case "FatigueDamage": return [e.player, `n'a plus de cartes : fatigue <b>${e.amount}</b>`];
      case "CardBurned": return [e.player, `main pleine, <b>${esc(e.card)}</b> est détruite`];
      case "IllegalAction": return [e.player, `action refusée : ${esc(e.reason)}`];
      case "BotSpoke": return e.message ? [e.player, `dit : <b>« ${esc(e.message)} »</b>`]
        : [e.player, `réfléchit : <i>${esc(e.thought)}</i>`];
      case "MatchEnded": return ["Fin du combat", e.winner === "DRAW" ? "<b>Match nul</b>"
        : `<b>${esc(e.winner)}</b> remporte la partie (${esc(e.reason)}, ${plural(e.rounds, "manche")})`];
      default: return null;
    }
  }

  Arena.Narration = { describe };
})(typeof window !== "undefined" ? window : globalThis);
