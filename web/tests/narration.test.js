(function (root) {
  const { test, eq, ok } = root.Arena.Test;
  const N = () => root.Arena.Narration;
  const state = (current, round) => ({ current, round });
  const I18n = () => root.Arena.I18n;

  /** Runs a check in a language without saving it, then puts the previous language back. */
  function inLanguage(lang, check) {
    const before = I18n().lang();
    I18n().setLang(lang, false);
    try {
      check();
    } finally {
      I18n().setLang(before, false);
    }
  }

  test("given_a_new_turn_after_another_player_when_narrated_in_french_then_it_says_whose_turn_ends_and_whose_begins", () => inLanguage("fr", () => {
    const line = N().describe({ type: "TurnStarted", round: 3, player: "Bob", hp: 24, armor: 2, handSize: 4, deckSize: 12 },
      state("Alice", 3));
    eq(line[0], "Fin du tour de Alice");
    ok(line[1].includes("À <b>Bob</b> de jouer"), line[1]);
    ok(line[1].includes("manche 3"), line[1]);
  }));

  test("given_a_new_turn_when_narrated_in_english_then_the_message_is_in_english", () => inLanguage("en", () => {
    const line = N().describe({ type: "TurnStarted", round: 3, player: "Bob", hp: 24, armor: 2, handSize: 4, deckSize: 12 },
      state("Alice", 3));
    eq(line[0], "End of Alice's turn");
    ok(line[1].includes("<b>Bob</b> to play"), line[1]);
  }));

  test("given_the_coin_flip_when_narrated_then_it_names_who_starts_and_who_gets_the_coin", () => {
    const line = N().describe({ type: "FirstPlayerChosen", first: "Alice", second: "Bob" }, state(null, 0));
    ok(line[1].includes("Alice") && line[1].includes("Bob") && line[1].includes("The Coin"), line[1]);
  });

  test("given_every_event_type_of_the_sample_match_when_narrated_then_each_one_gets_a_message", () => {
    const events = root.Arena.Replay.parseJsonl(root.ArenaSamples.matchJsonl);
    const timeline = root.Arena.Replay.buildTimeline(events);
    const silent = timeline.frames.filter((f, i) => !N().describe(f.event, i ? timeline.frames[i - 1].state : null))
      .map((f) => f.event.type);
    eq([...new Set(silent)], []);
  });

  test("given_a_card_name_with_html_when_narrated_then_it_is_escaped", () => {
    const line = N().describe({ type: "CardPlayed", player: "A", card: "<b>x</b>", cost: 1, manaLeft: 0 }, state("A", 1));
    ok(!line[1].includes("<b>x</b>") && line[1].includes("&lt;b&gt;"), line[1]);
  });

  test("given_a_resource_card_when_its_mana_is_gained_then_the_message_names_the_player", () => {
    const line = N().describe({ type: "ManaGained", player: "Alice", source: "Mana Crystal", mana: 0, maxMana: 2 }, state("Alice", 2));
    eq(line[0], "Alice");
    ok(line[1].includes("Mana Crystal"), line[1]);
  });
})(typeof window !== "undefined" ? window : globalThis);
