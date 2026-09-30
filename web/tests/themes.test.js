(function (root) {
  const { test, eq } = root.Arena.Test;
  const Themes = () => root.Arena.Themes;

  test("given_the_classic_theme_when_drawing_a_card_then_it_keeps_its_emoji", () => {
    eq(Themes().artFor("classic", "card", "Fireball", "ATTACK"), { emoji: "🔥" });
  });

  test("given_the_classic_theme_when_drawing_a_hero_then_it_keeps_its_class_emoji", () => {
    eq(Themes().artFor("classic", "hero", "Mage"), { emoji: "🔮" });
  });

  test("given_the_grimoire_theme_when_drawing_a_card_then_it_uses_its_svg_icon", () => {
    eq(Themes().artFor("grimoire", "card", "Fireball", "ATTACK"), { src: "assets/grimoire/fireball.svg" });
  });

  test("given_the_grimoire_theme_and_an_unknown_card_when_drawing_it_then_its_category_icon_is_used", () => {
    eq(Themes().artFor("grimoire", "card", "Mystery Card", "DEFENSE"), { src: "assets/grimoire/shield.svg" });
  });

  test("given_a_theme_without_art_for_a_card_when_drawing_it_then_the_classic_emoji_is_the_fallback", () => {
    eq(Themes().artFor("plein-air", "card", "Fireball", "ATTACK"), { emoji: "🔥" });
  });

  test("given_the_plein_air_theme_when_drawing_a_hero_then_it_uses_its_png_tile", () => {
    eq(Themes().artFor("plein-air", "hero", "Tank"), { src: "assets/plein-air/hero-tank.png" });
  });

  test("given_every_theme_when_drawing_any_class_then_there_is_always_some_art", () => {
    for (const theme of Themes().THEMES) {
      for (const heroClass of ["Mage", "Tank", "Swordsman", "Assassin", "Cleric"]) {
        const art = Themes().artFor(theme.id, "hero", heroClass);
        eq(Boolean(art.src || art.emoji), true);
      }
    }
  });

  test("given_an_unknown_theme_id_when_looking_it_up_then_the_classic_theme_is_used", () => {
    eq(Themes().themeById("nope").id, "classic");
  });

  test("given_art_with_an_image_when_writing_html_then_it_is_a_decorative_img", () => {
    eq(Themes().artHtml("grimoire", "card", "Fireball", "ATTACK"),
      '<img class="art" src="assets/grimoire/fireball.svg" alt="" draggable="false">');
  });

  test("given_art_with_an_emoji_when_writing_html_then_it_is_the_emoji_itself", () => {
    eq(Themes().artHtml("classic", "card", "Fireball", "ATTACK"), "🔥");
  });
})(globalThis);
