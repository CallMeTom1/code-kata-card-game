(function (root) {
  const { test, eq } = root.Arena.Test;
  const cue = (event) => root.Arena.Sound.cueFor(event);

  test("given_a_big_hit_when_choosing_a_sound_then_it_is_a_heavy_hit", () => {
    eq(cue({ type: "DamageDealt", amount: 8, absorbed: 0, hpBefore: 30, hpAfter: 22 }), { name: "hit", intensity: 1 });
  });

  test("given_a_small_hit_when_choosing_a_sound_then_it_is_a_light_hit", () => {
    eq(cue({ type: "DamageDealt", amount: 2, absorbed: 0, hpBefore: 30, hpAfter: 28 }), { name: "hit", intensity: 0.4 });
  });

  test("given_a_hit_fully_absorbed_by_armor_when_choosing_a_sound_then_it_clanks_on_the_shield", () => {
    eq(cue({ type: "DamageDealt", amount: 4, absorbed: 4, hpBefore: 30, hpAfter: 30 }), { name: "block", intensity: 0.5 });
  });

  test("given_statuses_when_choosing_a_sound_then_each_keyword_has_its_own_sound", () => {
    eq(cue({ type: "StatusApplied", status: "FROZEN (-1 mana next turn)" }).name, "freeze");
    eq(cue({ type: "StatusApplied", status: "POISON 2 (3 turns)" }).name, "poison");
    eq(cue({ type: "StatusApplied", status: "EVASION" }).name, "shimmer");
    eq(cue({ type: "StatusApplied", status: "NEXT ATTACK +3" }).name, "shimmer");
  });

  test("given_the_end_of_a_match_when_choosing_a_sound_then_a_win_plays_a_fanfare_and_a_draw_a_softer_one", () => {
    eq(cue({ type: "MatchEnded", winner: "Bob" }).name, "fanfare");
    eq(cue({ type: "MatchEnded", winner: "DRAW" }).name, "draw-game");
  });

  test("given_quiet_events_when_choosing_a_sound_then_there_is_none", () => {
    eq(cue({ type: "ManaRefilled" }), null);
    eq(cue({ type: "PlayerSetUp" }), null);
    eq(cue({ type: "Unknown" }), null);
  });

  test("given_minor_sounds_when_played_fast_then_they_are_marked_skippable", () => {
    eq(root.Arena.Sound.isMinor("draw"), true);
    eq(root.Arena.Sound.isMinor("hit"), false);
  });
})(typeof window !== "undefined" ? window : globalThis);
