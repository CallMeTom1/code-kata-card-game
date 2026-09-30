(function (root) {
  const { test, eq, ok } = root.Arena.Test;
  const V = () => root.Arena.Visuals;
  const R = () => root.Arena.Replay;

  const START = [
    { type: "MatchStarted", player1: "Alice", player1Label: "Aggressive:Mage", player2: "Bob", player2Label: "Defensive:Tank", seed: 1 },
    { type: "PlayerSetUp", player: "Alice", bot: "Aggressive", heroClass: "Mage", classChoice: "imposed", deckSource: "built",
      heroPower: "Fireblast", heroPowerCost: 2, heroPowerText: "deal 1 damage",
      deck: [{ name: "Fireball", cost: 4, category: "ATTACK", text: "Deal 6 damage." }] },
    { type: "PlayerSetUp", player: "Bob", bot: "Defensive", heroClass: "Tank", classChoice: "imposed", deckSource: "built",
      heroPower: "Armor Up", heroPowerCost: 2, heroPowerText: "Armor 2 (3 turns)", deck: [] },
    { type: "OpeningHand", player: "Alice", cards: ["Fireball"] },
    { type: "TurnStarted", round: 1, player: "Alice", hp: 30, armor: 0, handSize: 1, deckSize: 0 },
  ];

  function states(events) {
    let state = R().initialState();
    const all = [state];
    events.forEach((e) => { state = R().apply(state, e); all.push(state); });
    return all;
  }

  function effects(extra) {
    const s = states([...START, ...extra]);
    return V().effectsFor(extra.at(-1), s.at(-2), s.at(-1));
  }

  test("given_known_cards_and_minions_when_asking_icons_then_each_has_its_own", () => {
    eq(V().iconFor("Fireball"), "🔥");
    eq(V().iconFor("Skeleton"), "💀");
    eq(V().iconFor("Iron Golem"), "🗿");
  });

  test("given_an_unknown_card_when_asking_its_icon_then_the_category_icon_is_used", () => {
    eq(V().iconFor("Mystery", "DEFENSE"), "🛡️");
    eq(V().iconFor("Mystery"), "🎴");
  });

  test("given_a_card_played_when_choosing_effects_then_the_card_is_revealed_with_its_text", () => {
    const fx = effects([{ type: "CardPlayed", player: "Alice", card: "Fireball", cost: 4, manaLeft: 0 }]);
    eq(fx, [{ kind: "reveal", player: "Alice",
      card: { name: "Fireball", cost: 4, category: "ATTACK", text: "Deal 6 damage.", icon: "🔥" } }]);
  });

  test("given_fireball_damage_when_choosing_effects_then_a_fire_projectile_flies_then_a_heavy_impact", () => {
    const fx = effects([{ type: "DamageDealt", source: "Fireball", target: "Bob", amount: 6, absorbed: 0, hpBefore: 30, hpAfter: 24, armorAfter: 0 }]);
    eq(fx, [
      { kind: "projectile", style: "fire", from: { hero: "Alice" }, to: { hero: "Bob" } },
      { kind: "impact", target: { hero: "Bob" }, amount: 6, heavy: true, delayed: true },
    ]);
  });

  test("given_a_hit_fully_absorbed_when_choosing_effects_then_the_impact_is_a_block", () => {
    const fx = effects([{ type: "DamageDealt", source: "Strike", target: "Bob", amount: 4, absorbed: 4, hpBefore: 30, hpAfter: 30, armorAfter: 1 }]);
    eq(fx[1], { kind: "block", target: { hero: "Bob" }, delayed: true });
  });

  test("given_holy_nova_when_choosing_effects_then_a_wave_leaves_the_caster_instead_of_a_projectile", () => {
    const fx = effects([{ type: "DamageDealt", source: "Holy Nova", target: "Bob", amount: 2, absorbed: 0, hpBefore: 30, hpAfter: 28, armorAfter: 0 }]);
    eq(fx[0], { kind: "wave", style: "holy", from: { hero: "Alice" } });
  });

  test("given_a_minion_attack_then_its_hit_when_choosing_effects_then_the_minion_lunges_and_the_hit_has_no_projectile", () => {
    const extra = [
      { type: "MinionSummoned", owner: "Alice", minion: "Wolf", attack: 2, health: 2, taunt: false, boardSize: 1 },
      { type: "MinionAttacked", owner: "Alice", minion: "Wolf", attack: 2, health: 2, target: "Bob" },
      { type: "DamageDealt", source: "Wolf", target: "Bob", amount: 2, absorbed: 0, hpBefore: 30, hpAfter: 28, armorAfter: 0 },
    ];
    const s = states([...START, ...extra]);
    eq(V().effectsFor(extra[1], s.at(-3), s.at(-2)), [{ kind: "lunge", minionId: 1, to: { hero: "Bob" } }]);
    eq(V().effectsFor(extra[2], s.at(-2), s.at(-1)), [{ kind: "impact", target: { hero: "Bob" }, amount: 2, heavy: false, delayed: false }]);
  });

  test("given_a_summon_and_a_death_when_choosing_effects_then_the_right_minion_is_targeted", () => {
    const extra = [
      { type: "MinionSummoned", owner: "Bob", minion: "Golem", attack: 0, health: 6, taunt: true, boardSize: 1 },
      { type: "MinionDamaged", owner: "Bob", minion: "Golem", source: "Wolf", amount: 6, healthLeft: 0 },
      { type: "MinionDied", owner: "Bob", minion: "Golem" },
    ];
    const s = states([...START, ...extra]);
    eq(V().effectsFor(extra[0], s.at(-4), s.at(-3)), [{ kind: "summon", minionId: 1 }]);
    eq(V().effectsFor(extra[2], s.at(-2), s.at(-1)), [{ kind: "death", minionId: 1 }]);
  });

  test("given_a_turn_start_and_statuses_when_choosing_effects_then_banner_and_auras_are_shown", () => {
    eq(effects([{ type: "TurnStarted", round: 2, player: "Bob", hp: 30, armor: 0, handSize: 0, deckSize: 0 }]),
      [{ kind: "banner", text: "Tour 2 · Bob", player: "Bob" }]);
    eq(effects([{ type: "StatusApplied", target: "Bob", source: "Frostbolt", status: "FROZEN (-1 mana next turn)" }]),
      [{ kind: "aura", style: "frost", target: { hero: "Bob" } }]);
    eq(effects([{ type: "Healed", player: "Alice", source: "Potion", amount: 4, hpBefore: 20, hpAfter: 24 }]),
      [{ kind: "aura", style: "heal", target: { hero: "Alice" } }]);
  });

  test("given_a_win_when_choosing_effects_then_the_loser_breaks_and_the_winner_shines", () => {
    eq(effects([{ type: "MatchEnded", winner: "Bob", reason: "HP 0", rounds: 9, player1: "Alice", hp1: 0, damage1: 1, player2: "Bob", hp2: 3, damage2: 30 }]),
      [{ kind: "victory", winner: "Bob", loser: "Alice" }]);
  });

  test("given_a_timeline_when_building_the_hp_curve_then_there_is_one_point_per_turn_start_plus_the_end", () => {
    const events = R().parseJsonl(root.ArenaSamples.matchJsonl);
    const timeline = R().buildTimeline(events);
    const curve = V().hpCurve(timeline);
    eq(curve.players, timeline.frames.at(-1).state.order);
    eq(curve.points.length, timeline.turnStarts.length + 1);
    eq(curve.points[0].hp, [30, 30]);
    const last = timeline.frames.at(-1).state;
    eq(curve.points.at(-1).hp, [last.players[curve.players[0]].hp, last.players[curve.players[1]].hp]);
    ok(curve.points.every((p, i) => i === 0 || p.frame > curve.points[i - 1].frame), "frames increase");
  });
})(typeof window !== "undefined" ? window : globalThis);
