/*
 * Visual language of the replay: icons for cards and minions, and which effects an event shows
 * (card reveal, projectile, impact, lunge, summon, death, auras, turn banner, victory).
 * Pure functions only: fx.js draws them, tests.html checks them.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});

  const ICONS = {
    "Quick Jab": "👊", Strike: "🥊", "Crushing Blow": "🔨", "Wild Wolf": "🐺", Wolf: "🐺", "Wooden Shield": "🪵",
    "Iron Wall": "🧱", Shieldbearer: "🛡️", "Mana Crystal": "💎", Insight: "👁️", "Healing Potion": "🧪", "The Coin": "🪙",
    Frostbolt: "❄️", Fireball: "🔥", Pyroblast: "☄️", "Raise Skeletons": "💀", Skeleton: "💀", "Ice Barrier": "🧊",
    "Arcane Intellect": "📖", "Shield Slam": "💥", "Shield Block": "🔰", Fortress: "🏰", "Iron Golem": "🗿",
    "War Chest": "💰", "Last Stand": "❤️", "Twin Blades": "⚔️", "Whirlwind Slash": "🌪️", Riposte: "🤺",
    "Focus Training": "🎯", "Battle Cry": "📯", Backstab: "🔪", Eviscerate: "🩸", "Deadly Poison": "☠️",
    "Venom Spider": "🕷️", Evasion: "💨", Preparation: "⏳", Smite: "⚡", "Holy Nova": "🌟",
    "Power Word: Shield": "🔰", "Divine Blessing": "🙏", "Greater Heal": "💖", "Spirit Healer": "👼",
  };
  const CATEGORY_ICONS = { ATTACK: "⚔️", DEFENSE: "🛡️", RESOURCE: "💎", UTILITY: "✨" };

  const STYLES = {
    Fireball: "fire", Pyroblast: "fire", Fireblast: "fire", Frostbolt: "frost", Smite: "holy", "Holy Nova": "holy",
    "Twin Blades": "blade", Riposte: "blade", Backstab: "blade", Eviscerate: "blade", "Whirlwind Slash": "blade",
    Strike: "strike", "Quick Jab": "strike", "Crushing Blow": "strike", "Shield Slam": "shield",
  };
  const AREA = new Set(["Holy Nova", "Whirlwind Slash"]);

  /** Icon of a card or minion; unknown names fall back to their category, then to a generic card. */
  function iconFor(name, category) {
    return ICONS[name] || CATEGORY_ICONS[category] || "🎴";
  }

  function other(state, name) {
    return state.order.find((n) => n !== name);
  }

  function fromMinion(before, source) {
    const h = before && before.highlight;
    return !!(h && h.kind === "minion-attack" && before.players[h.player].board.some((m) => m.name === source));
  }

  function hit(target, lost, delayed) {
    return lost > 0 ? { kind: "impact", target, amount: lost, heavy: lost >= 6, delayed } : { kind: "block", target, delayed };
  }

  function statusStyle(status) {
    if (status.startsWith("FROZEN")) return "frost";
    if (status.startsWith("POISON")) return "poison";
    if (status.startsWith("EVASION")) return "evade";
    return "buff";
  }

  /** The effects to draw for one event, given the states just before and just after it. */
  function effectsFor(event, before, after) {
    switch (event.type) {
      case "TurnStarted":
        return [{ kind: "banner", text: `Tour ${event.round} · ${event.player}`, player: event.player }];
      case "ManaRefilled": return [{ kind: "mana", player: event.player }];
      case "CardDrawn": return [{ kind: "draw", player: event.player }];
      case "HeroPowerUsed": return [{ kind: "power", player: event.player }];
      case "CardPlayed": {
        const player = before.players[event.player];
        const card = player.hand.find((c) => c.name === event.card)
          || { name: event.card, cost: event.cost, ...(player.catalog[event.card] || {}) };
        return [{ kind: "reveal", player: event.player, card: { name: card.name, cost: card.cost, category: card.category,
          text: card.text || "", icon: iconFor(card.name, card.category) } }];
      }
      case "DamageDealt": {
        const target = { hero: event.target };
        const lost = event.hpBefore - event.hpAfter;
        if (fromMinion(before, event.source)) return [hit(target, lost, false)];
        const caster = { hero: other(after, event.target) };
        const style = STYLES[event.source] || "arcane";
        const travel = AREA.has(event.source) ? { kind: "wave", style, from: caster }
          : { kind: "projectile", style, from: caster, to: target };
        return [travel, hit(target, lost, true)];
      }
      case "MinionDamaged": {
        const id = after.highlight && after.highlight.minionId;
        const trade = before.highlight && before.highlight.kind === "minion-attack";
        return id ? [{ kind: "impact", target: { minionId: id }, amount: event.amount, heavy: false, delayed: !trade }] : [];
      }
      case "MinionAttacked": {
        const id = after.highlight && after.highlight.minionId;
        let to = { hero: event.target };
        if (!after.players[event.target]) {
          const board = after.players[other(after, event.owner)].board;
          const blocker = board.find((m) => m.name === event.target && m.taunt) || board.find((m) => m.name === event.target);
          to = { minionId: blocker && blocker.id };
        }
        return id ? [{ kind: "lunge", minionId: id, to }] : [];
      }
      case "MinionSummoned":
        return [{ kind: "summon", minionId: after.highlight.minionId }];
      case "MinionDied": {
        const board = before.players[event.owner].board;
        const dead = board.find((m) => m.name === event.minion && m.health <= 0) || board.find((m) => m.name === event.minion);
        return dead ? [{ kind: "death", minionId: dead.id }] : [];
      }
      case "Healed": return event.amount > 0 ? [{ kind: "aura", style: "heal", target: { hero: event.player } }] : [];
      case "ArmorGained": return [{ kind: "aura", style: "armor", target: { hero: event.player } }];
      case "StatusApplied": return [{ kind: "aura", style: statusStyle(event.status), target: { hero: event.target } }];
      case "EvasionTriggered": return [{ kind: "aura", style: "evade", target: { hero: event.player } }];
      case "PoisonTicked":
        return [{ kind: "aura", style: "poison", target: { hero: event.player } },
          hit({ hero: event.player }, event.hpBefore - event.hpAfter, false)];
      case "FatigueDamage": return [hit({ hero: event.player }, event.hpBefore - event.hpAfter, false)];
      case "MatchEnded":
        return event.winner === "DRAW" ? [] : [{ kind: "victory", winner: event.winner, loser: other(after, event.winner) }];
      default:
        return [];
    }
  }

  /** HP of both players at the start of every turn and at the end, plus the big hits, for the HP chart. */
  function hpCurve(timeline) {
    const last = timeline.frames.at(-1).state;
    const players = last.order;
    const point = (frame) => {
      const s = timeline.frames[frame].state;
      return { frame, round: s.round, player: s.current, hp: players.map((p) => s.players[p].hp) };
    };
    const points = timeline.turnStarts.map(point);
    points.push(point(timeline.frames.length - 1));
    const bigHits = timeline.frames
      .filter((f) => f.event.type === "DamageDealt" && f.event.hpBefore - f.event.hpAfter >= 6)
      .map((f) => ({ frame: f.index, target: f.event.target, amount: f.event.hpBefore - f.event.hpAfter, source: f.event.source }));
    return { players, points, bigHits };
  }

  Arena.Visuals = { iconFor, effectsFor, hpCurve };
})(typeof window !== "undefined" ? window : globalThis);
