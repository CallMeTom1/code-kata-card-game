/*
 * The same bracket-tagged lines as the Java ConsoleRenderer, so the web log reads like the console.
 * A test compares this output with docs/sample-match.log line by line.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const CATEGORIES = ["ATTACK", "DEFENSE", "RESOURCE", "UTILITY"];

  /** Java's %.1f rounds half-up on the shortest decimal form; toFixed would not always agree. */
  function oneDecimal(x) {
    const [whole, fraction = ""] = String(x).split(".");
    let tenths = Number(whole) * 10 + Number(fraction[0] || 0);
    if (Number(fraction[1] || 0) >= 5) tenths += 1;
    return Math.floor(tenths / 10) + "." + (tenths % 10);
  }

  function curve(deck) {
    const buckets = [0, 0, 0, 0, 0, 0];
    deck.forEach((c) => buckets[Math.min(c.cost, 5)]++);
    let text = "";
    for (let cost = 0; cost <= 5; cost++) {
      if (cost === 0 && buckets[0] === 0) continue;
      text += "[" + (cost === 5 ? "5+" : cost) + ": " + buckets[cost] + "] ";
    }
    const average = deck.length ? deck.reduce((sum, c) => sum + c.cost, 0) / deck.length : 0;
    return text + "| avg cost " + oneDecimal(average);
  }

  /** Returns a formatter: event in, zero or more log lines out. It keeps the little context it needs. */
  function create() {
    let nameWidth = 2;
    let round = 0;
    let current = "";
    const maxMana = {};

    const pad = (name) => name.padEnd(nameWidth);
    const roundTag = () => "[T" + String(round).padStart(2, "0") + "]";
    const log = (player, tag, text) => {
      const tagText = "[" + tag.padEnd(7) + "]";
      const name = "[" + pad(player) + "]";
      return round > 0 ? roundTag() + name + tagText + " " + text : tagText + name + " " + text;
    };
    const act = (tag, text) => log(current, tag, text);
    const mana = (player, left) => "[MANA " + left + "/" + (player in maxMana ? maxMana[player] : left) + "]";

    return function format(e) {
      switch (e.type) {
        case "MatchStarted":
          nameWidth = Math.max(e.player1.length, e.player2.length);
          return ["[MATCH  ] " + e.player1 + " [" + e.player1Label + "] vs " + e.player2 + " [" + e.player2Label
            + "] | seed " + e.seed];
        case "PlayerSetUp": {
          const lines = [
            log(e.player, "SETUP", "[" + e.bot + "] plays [" + e.heroClass + "] (" + e.classChoice + ") | deck: "
              + e.deckSource),
            log(e.player, "POWER", e.heroPower + " (" + e.heroPowerCost + ")" + (e.heroPowerText ? " — " + e.heroPowerText : "")),
          ];
          CATEGORIES.forEach((category) => {
            const counts = new Map();
            e.deck.filter((c) => c.category === category).forEach((c) => counts.set(c.name, (counts.get(c.name) || 0) + 1));
            if (counts.size) {
              const total = [...counts.values()].reduce((a, b) => a + b, 0);
              const list = [...counts].map(([name, n]) => name + " x" + n).join(", ");
              lines.push(log(e.player, "DECK", "[" + category.padEnd(8) + " " + String(total).padStart(2) + "] " + list));
            }
          });
          lines.push(log(e.player, "CURVE", curve(e.deck)));
          return lines;
        }
        case "FirstPlayerChosen":
          return ["[START  ] " + e.first + " goes first (coin flip) | " + e.second + " gets The Coin"];
        case "MulliganDone":
          return [log(e.player, "SWAP", e.putBack.length === 0 ? "keeps the whole hand"
            : "puts back " + e.putBack.join(", ") + " → draws " + e.drawn.join(", "))];
        case "OpeningHand":
          return [log(e.player, "HAND", e.cards.join(", "))];
        case "TurnStarted": {
          const lines = [];
          if (e.round !== round) {
            round = e.round;
            lines.push(roundTag() + " " + "=".repeat(70));
          }
          current = e.player;
          lines.push(act("TURN", "[HP " + e.hp + "/30] [ARMOR " + e.armor + "] [HAND " + e.handSize + "] [DECK "
            + e.deckSize + "]"));
          return lines;
        }
        case "ManaRefilled":
          maxMana[e.player] = e.maxMana;
          return [act("MANA", "[MANA " + e.mana + "/" + e.maxMana + "]" + (e.frozen ? " [FROZEN -1]" : ""))];
        case "CardDrawn": return [act("DRAW", e.card)];
        case "CardBurned": return [act("BURN", e.card + " (hand full, card destroyed)")];
        case "FatigueDamage": return [act("FATIGUE", e.amount + " dmg [HP " + e.hpBefore + " → " + e.hpAfter + "]")];
        case "ArmorExpired": return [act("ARMOR", e.amount + " armor expired → [ARMOR " + e.armorLeft + "]")];
        case "PoisonTicked": return [act("POISON", e.amount + " dmg [HP " + e.hpBefore + " → " + e.hpAfter + "]")];
        case "CardPlayed": return [act("PLAY", e.card + " (" + e.cost + ") → " + mana(e.player, e.manaLeft))];
        case "HeroPowerUsed": return [act("POWER", e.power + " (" + e.cost + ") → " + mana(e.player, e.manaLeft))];
        case "IllegalAction": return [act("ILLEGAL", e.reason + " → turn ends")];
        case "DamageDealt":
          return [act("DAMAGE", e.source + " → " + e.target + " : " + e.amount + " dmg [ABSORBED " + e.absorbed
            + "] [HP " + e.hpBefore + " → " + e.hpAfter + "]")];
        case "EvasionTriggered":
          return [act("EVADE", e.player + "'s Evasion prevents " + e.prevented + " dmg from " + e.source)];
        case "Healed":
          return [act("HEAL", e.source + " → " + e.player + " : +" + e.amount + " [HP " + e.hpBefore + " → " + e.hpAfter + "]")];
        case "ArmorGained":
          return [act("ARMOR", e.source + " → " + e.player + " : +" + e.amount + " (" + e.turns + " turns) [ARMOR "
            + e.totalArmor + "]")];
        case "ManaGained":
          maxMana[e.player] = e.maxMana;
          return [act("MANA", e.source + " → [MANA " + e.mana + "/" + e.maxMana + "]")];
        case "StatusApplied": return [act("STATUS", e.target + " [" + e.status + "] from " + e.source)];
        case "MinionSummoned":
          return [act("SUMMON", e.minion + " [" + e.attack + "/" + e.health + "]" + (e.taunt ? " [TAUNT]" : "")
            + " → [BOARD " + e.boardSize + "/7]")];
        case "SummonFizzled": return [act("SUMMON", e.minion + " fizzles (board full)")];
        case "MinionAttacked":
          return [act("MINION", e.minion + " [" + e.attack + "/" + e.health + "] attacks " + e.target)];
        case "MinionDamaged":
          return [act("MINION", e.minion + " (" + e.owner + ") takes " + e.amount + " from " + e.source + " [HEALTH "
            + e.healthLeft + "]")];
        case "MinionDied": return [act("DEATH", e.minion + " (" + e.owner + ")")];
        case "MatchEnded":
          return ["[RESULT ] " + (e.winner === "DRAW" ? "DRAW" : e.winner + " WINS") + " | reason: " + e.reason
            + " | turns: " + e.rounds + " | HP " + e.player1 + " " + e.hp1 + " / " + e.player2 + " " + e.hp2
            + " | damage " + e.player1 + " " + e.damage1 + " / " + e.player2 + " " + e.damage2];
        default:
          return [];
      }
    };
  }

  Arena.LogFormat = { create, oneDecimal };
})(typeof window !== "undefined" ? window : globalThis);
