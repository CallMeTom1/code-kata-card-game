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

(function (root) {
  const { test, eq, ok } = root.Arena.Test;
  const S = () => root.Arena.Sound;
  const state = (phase, hp1, hp2) => ({ phase, order: ["A", "B"], players: { A: { hp: hp1 }, B: { hp: hp2 } } });

  test("given_the_setup_screen_when_choosing_the_music_level_then_only_the_calm_layer_plays", () => {
    eq(S().musicLevelFor(state("setup", 30, 30)), 0);
  });

  test("given_a_match_in_progress_when_both_are_healthy_then_the_battle_layer_plays", () => {
    eq(S().musicLevelFor(state("play", 25, 18)), 1);
  });

  test("given_a_champion_at_10_hp_or_less_when_choosing_the_music_level_then_the_climax_plays", () => {
    eq(S().musicLevelFor(state("play", 25, 10)), 2);
  });

  test("given_the_end_of_the_match_when_choosing_the_music_level_then_the_music_stops", () => {
    eq(S().musicLevelFor(state("ended", 0, 12)), -1);
    eq(S().musicLevelFor(null), -1);
  });

  test("given_note_names_when_converted_then_frequencies_follow_equal_temperament", () => {
    eq(S().noteFrequency("A4"), 440);
    ok(Math.abs(S().noteFrequency("D3") - 146.83) < 0.01, "D3");
    ok(Math.abs(S().noteFrequency("Bb2") - 116.54) < 0.01, "Bb2");
  });
})(typeof window !== "undefined" ? window : globalThis);

(function (root) {
  const { test, eq, ok } = root.Arena.Test;
  const S = () => root.Arena.Sound;

  test("given_the_music_themes_when_listed_then_there_are_at_least_4_with_unique_ids_and_names", () => {
    const themes = S().MUSIC_THEMES;
    ok(themes.length >= 4, "at least 4 themes");
    eq(new Set(themes.map((t) => t.id)).size, themes.length);
    ok(themes.every((t) => t.name && t.tempo > 0), "names and tempos");
  });

  test("given_every_theme_when_reading_its_notes_then_each_note_is_a_valid_pitch", () => {
    S().MUSIC_THEMES.forEach((t) => {
      const notes = [...t.chords.flat(), ...t.bass, ...t.melody.map(([note]) => note)];
      notes.forEach((note) => ok(S().noteFrequency(note) > 20, t.id + " " + note));
    });
  });

  test("given_every_theme_when_adding_its_melody_lengths_then_it_fills_whole_bars_of_its_progression", () => {
    S().MUSIC_THEMES.forEach((t) => {
      const eighths = t.melody.reduce((sum, [, length]) => sum + length, 0);
      eq(eighths % 8, 0, t.id + " melody length");
      eq(t.bass.length, t.chords.length, t.id + " one bass note per chord");
    });
  });

  test("given_an_unknown_theme_id_when_looking_it_up_then_the_epic_theme_is_used", () => {
    eq(S().musicTheme("tavern").id, "tavern");
    eq(S().musicTheme("does-not-exist").id, "epic");
  });
})(typeof window !== "undefined" ? window : globalThis);
