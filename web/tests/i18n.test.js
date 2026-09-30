(function (root) {
  const { test, eq, ok } = root.Arena.Test;
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

  test("given_no_saved_choice_when_the_page_starts_then_the_default_language_is_english", () => {
    eq(I18n().DEFAULT, "en");
    eq(I18n().LANGUAGES.includes("en") && I18n().LANGUAGES.includes("fr"), true);
  });

  test("given_english_when_translating_then_the_english_text_is_returned", () => {
    inLanguage("en", () => eq(I18n().t("tab.stats"), "Statistics"));
  });

  test("given_french_when_translating_then_the_french_text_is_returned", () => {
    inLanguage("fr", () => eq(I18n().t("tab.stats"), "Statistiques"));
  });

  test("given_a_text_with_placeholders_when_translating_then_the_values_are_filled_in", () => {
    inLanguage("en", () => eq(I18n().t("banner.turn", { round: 3, player: "Bob" }), "Turn 3 · Bob"));
    inLanguage("fr", () => eq(I18n().t("banner.turn", { round: 3, player: "Bob" }), "Tour 3 · Bob"));
  });

  test("given_a_zero_value_when_translating_then_it_is_kept", () => {
    inLanguage("en", () => eq(I18n().t("mana.title", { mana: 0, max: 1 }), "Mana 0/1"));
  });

  test("given_an_unknown_key_when_translating_then_the_fallback_or_the_key_is_returned", () => {
    eq(I18n().t("no.such.key"), "no.such.key");
    eq(I18n().t("no.such.key", null, "fallback"), "fallback");
  });

  test("given_an_unknown_language_when_switching_then_the_language_does_not_change", () => {
    const before = I18n().lang();
    I18n().setLang("xx", false);
    eq(I18n().lang(), before);
  });

  test("given_a_language_change_when_switching_then_listeners_are_told", () => {
    const heard = [];
    I18n().onChange((lang) => heard.push(lang));
    inLanguage(I18n().lang() === "fr" ? "en" : "fr", () => ok(heard.length === 1, "one call"));
  });

  test("given_both_languages_when_comparing_their_keys_then_they_are_the_same", () => {
    const { en, fr } = I18n().DICTIONARY;
    eq(Object.keys(fr).filter((k) => !(k in en)), []);
    eq(Object.keys(en).filter((k) => !(k in fr)), []);
  });

  test("given_both_languages_when_comparing_placeholders_then_each_text_uses_the_same_ones", () => {
    const { en, fr } = I18n().DICTIONARY;
    const names = (text) => (text.match(/\{\w+\}/g) || []).sort();
    Object.keys(en).forEach((key) => eq(names(fr[key]), names(en[key]), key));
  });

  test("given_every_design_and_music_theme_when_listing_them_then_each_has_a_translated_name", () => {
    const { en, fr } = I18n().DICTIONARY;
    root.Arena.Themes.THEMES.forEach((theme) => {
      ok(("design." + theme.id) in en && ("design." + theme.id) in fr, "design " + theme.id);
      if (theme.credits) ok(("credits." + theme.id) in en && ("credits." + theme.id) in fr, "credits " + theme.id);
    });
    root.Arena.Sound.MUSIC_THEMES.forEach((theme) => {
      ok(("music." + theme.id) in en && ("music." + theme.id) in fr, "music " + theme.id);
    });
  });
})(globalThis);
