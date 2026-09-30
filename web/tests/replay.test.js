(function (root) {
  const { test, eq, ok, throws } = root.Arena.Test;
  const R = () => root.Arena.Replay;

  const START = [
    { type: "MatchStarted", player1: "Alice", player1Label: "Aggressive:Mage", player2: "Bob", player2Label: "Defensive:Tank", seed: 42 },
    { type: "PlayerSetUp", player: "Alice", bot: "Aggressive", heroClass: "Mage", classChoice: "imposed", deckSource: "built",
      heroPower: "Fireblast", heroPowerCost: 2, heroPowerText: "deal 1 damage",
      deck: [{ name: "Strike", cost: 2, category: "ATTACK" }, { name: "Strike", cost: 2, category: "ATTACK" },
             { name: "Insight", cost: 1, category: "UTILITY" }] },
    { type: "PlayerSetUp", player: "Bob", bot: "Defensive", heroClass: "Tank", classChoice: "imposed", deckSource: "built",
      heroPower: "Armor Up", heroPowerCost: 2, heroPowerText: "Armor 2 (3 turns)",
      deck: [{ name: "Iron Wall", cost: 3, category: "DEFENSE" }, { name: "Wild Wolf", cost: 2, category: "ATTACK" }] },
  ];

  function replay(events) {
    return events.reduce((state, event) => R().apply(state, event), R().initialState());
  }

  test("given_jsonl_text_with_blank_lines_when_parsed_then_each_line_is_an_event", () => {
    const events = R().parseJsonl('{"type":"A"}\n\n{"type":"B"}\n');
    eq(events.map((e) => e.type), ["A", "B"]);
  });

  test("given_an_invalid_line_when_parsed_then_the_error_names_the_line", () => {
    throws(() => R().parseJsonl('{"type":"A"}\nnot json'), "line 2");
  });

  test("given_a_line_without_type_when_parsed_then_it_is_rejected", () => {
    throws(() => R().parseJsonl('{"player":"A"}'), "line 1");
  });

  test("given_setup_events_when_applied_then_both_players_start_at_30_hp_with_their_deck", () => {
    const state = replay(START);
    eq(state.order, ["Alice", "Bob"]);
    eq(state.players.Alice.hp, 30);
    eq(state.players.Alice.heroClass, "Mage");
    eq(state.players.Alice.deckCount, 3);
    eq(state.players.Bob.heroPower, "Armor Up");
  });

  test("given_an_opening_hand_with_the_coin_when_applied_then_cards_have_their_cost_and_the_deck_shrinks", () => {
    const state = replay([...START, { type: "OpeningHand", player: "Alice", cards: ["Strike", "The Coin"] }]);
    eq(state.players.Alice.hand, [
      { name: "Strike", cost: 2, category: "ATTACK", text: "" },
      { name: "The Coin", cost: 0, category: "RESOURCE", text: "Gain 1 mana this turn only." },
    ]);
    eq(state.players.Alice.deckCount, 2);
  });

  test("given_deck_entries_with_text_when_a_card_is_drawn_then_the_hand_card_keeps_its_text", () => {
    const setup = START.map((e) => (e.type === "PlayerSetUp" && e.player === "Alice"
      ? { ...e, deck: [{ name: "Strike", cost: 2, category: "ATTACK", text: "Deal 4 damage." }] } : e));
    const state = replay([...setup, { type: "OpeningHand", player: "Alice", cards: ["Strike"] }]);
    eq(state.players.Alice.hand[0].text, "Deal 4 damage.");
  });

  test("given_a_draw_then_a_play_when_applied_then_hand_deck_and_mana_follow", () => {
    const state = replay([...START,
      { type: "OpeningHand", player: "Alice", cards: [] },
      { type: "TurnStarted", round: 1, player: "Alice", hp: 30, armor: 0, handSize: 0, deckSize: 3 },
      { type: "CardDrawn", player: "Alice", card: "Strike" },
      { type: "ManaRefilled", player: "Alice", mana: 2, maxMana: 2, frozen: false },
      { type: "CardPlayed", player: "Alice", card: "Strike", cost: 2, manaLeft: 0 }]);
    const alice = state.players.Alice;
    eq(alice.hand, []);
    eq(alice.deckCount, 2);
    eq([alice.mana, alice.maxMana], [0, 2]);
    eq(state.round, 1);
    eq(state.current, "Alice");
    eq(state.highlight, { kind: "play", player: "Alice", card: "Strike" });
  });

  test("given_damage_on_bob_when_applied_then_his_hp_and_armor_come_from_the_event", () => {
    const state = replay([...START,
      { type: "DamageDealt", source: "Strike", target: "Bob", amount: 4, absorbed: 3, hpBefore: 30, hpAfter: 29, armorAfter: 2 }]);
    eq([state.players.Bob.hp, state.players.Bob.armor], [29, 2]);
    eq(state.highlight.kind, "damage");
    eq(state.highlight.target, "Bob");
  });

  test("given_two_skeletons_when_the_damaged_one_dies_then_the_healthy_one_stays", () => {
    const state = replay([...START,
      { type: "MinionSummoned", owner: "Alice", minion: "Skeleton", attack: 1, health: 1, taunt: false, boardSize: 1 },
      { type: "MinionSummoned", owner: "Alice", minion: "Golem", attack: 0, health: 6, taunt: true, boardSize: 2 },
      { type: "MinionSummoned", owner: "Alice", minion: "Golem", attack: 0, health: 6, taunt: true, boardSize: 3 },
      { type: "MinionDamaged", owner: "Alice", minion: "Golem", source: "Wolf", amount: 2, healthLeft: 4 },
      { type: "MinionDamaged", owner: "Alice", minion: "Golem", source: "Wolf", amount: 4, healthLeft: 0 },
      { type: "MinionDied", owner: "Alice", minion: "Golem" }]);
    const board = state.players.Alice.board;
    eq(board.map((m) => [m.name, m.health]), [["Skeleton", 1], ["Golem", 6]]);
    ok(board[1].taunt, "the remaining golem keeps Taunt");
  });

  test("given_poison_for_2_turns_when_it_ticks_twice_then_the_badge_is_gone", () => {
    const poisoned = replay([...START,
      { type: "StatusApplied", target: "Bob", source: "Deadly Poison", status: "POISON 2 (2 turns)" }]);
    eq(poisoned.players.Bob.poisons, [{ amount: 2, turns: 2 }]);
    const ticked = [
      { type: "PoisonTicked", player: "Bob", amount: 2, hpBefore: 30, hpAfter: 28 },
      { type: "PoisonTicked", player: "Bob", amount: 2, hpBefore: 28, hpAfter: 26 },
    ].reduce((s, e) => R().apply(s, e), poisoned);
    eq(ticked.players.Bob.poisons, []);
    eq(ticked.players.Bob.hp, 26);
  });

  test("given_statuses_when_applied_then_freeze_evasion_parry_and_buff_are_shown", () => {
    const state = replay([...START,
      { type: "StatusApplied", target: "Bob", source: "Frostbolt", status: "FROZEN (-1 mana next turn)" },
      { type: "StatusApplied", target: "Alice", source: "Evasion", status: "EVASION" },
      { type: "StatusApplied", target: "Alice", source: "Riposte", status: "PARRY 2 (1 turn)" },
      { type: "StatusApplied", target: "Alice", source: "Battle Cry", status: "NEXT ATTACK +3" }]);
    ok(state.players.Bob.frozen, "Bob frozen");
    ok(state.players.Alice.evasion, "Alice has Evasion");
    eq(state.players.Alice.parry, 2);
    eq(state.players.Alice.attackBuff, 3);
  });

  test("given_events_when_the_timeline_is_built_then_earlier_frames_are_never_modified", () => {
    const events = [...START,
      { type: "TurnStarted", round: 1, player: "Alice", hp: 30, armor: 0, handSize: 0, deckSize: 3 },
      { type: "DamageDealt", source: "Fireblast", target: "Bob", amount: 1, absorbed: 0, hpBefore: 30, hpAfter: 29, armorAfter: 0 },
      { type: "TurnStarted", round: 1, player: "Bob", hp: 29, armor: 0, handSize: 0, deckSize: 2 }];
    const timeline = R().buildTimeline(events);
    eq(timeline.frames.length, events.length);
    eq(timeline.turnStarts, [3, 5]);
    eq(timeline.frames[3].state.players.Bob.hp, 30);
    eq(timeline.frames[4].state.players.Bob.hp, 29);
  });

  test("given_the_sample_match_when_replayed_to_the_end_then_final_hp_match_the_result_event", () => {
    const events = R().parseJsonl(root.ArenaSamples.matchJsonl);
    const last = R().buildTimeline(events).frames.at(-1).state;
    eq(last.phase, "ended");
    eq(last.players[last.result.player1].hp, last.result.hp1);
    eq(last.players[last.result.player2].hp, last.result.hp2);
  });

  test("given_two_bots_talking_when_applied_then_each_keeps_its_last_words_and_the_dialogue_keeps_the_order", () => {
    const state = replay(START.concat([
      { type: "BotSpoke", player: "Alice", thought: "Burn him.", message: "Feel the heat!" },
      { type: "BotSpoke", player: "Bob", thought: "", message: "My armor laughs." },
    ]));
    eq(state.players.Alice.speech, { thought: "Burn him.", message: "Feel the heat!", round: 0 });
    eq(state.dialogue.map((d) => d.player + ": " + d.message), ["Alice: Feel the heat!", "Bob: My armor laughs."]);
    eq(state.highlight.kind, "speech");
  });
})(typeof window !== "undefined" ? window : globalThis);
