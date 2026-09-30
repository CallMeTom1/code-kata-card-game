/*
 * Design themes of the page: each one is a palette (in arena.css, keyed by its id) and optional art
 * (SVG, PNG or JPG files under web/assets/) for heroes and cards. Anything a theme has no art for
 * falls back to the classic emoji, so a theme can be added one picture at a time.
 * Pure functions only: view-replay.js and fx.js draw what artFor returns.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});

  const CLASS_EMOJIS = { Mage: "🔮", Tank: "🛡️", Swordsman: "⚔️", Assassin: "🗡️", Cleric: "✨" };

  const GRIMOIRE_CARDS = {
    "Quick Jab": "punch", Strike: "fist", "Crushing Blow": "warhammer", "Wild Wolf": "wolf-howl", Wolf: "wolf-head",
    "Wooden Shield": "wood-beam", "Iron Wall": "stone-wall", Shieldbearer: "spiked-shield", "Mana Crystal": "crystal-growth",
    Insight: "all-seeing-eye", "Healing Potion": "health-potion", "The Coin": "two-coins", Frostbolt: "ice-bolt",
    Fireball: "fireball", Pyroblast: "meteor-impact", "Raise Skeletons": "skeleton", Skeleton: "skeleton",
    "Ice Barrier": "ice-shield", "Arcane Intellect": "spell-book", "Shield Slam": "shield-bash", "Shield Block": "shield-reflect",
    Fortress: "stone-tower", "Iron Golem": "rock-golem", "War Chest": "open-treasure-chest", "Last Stand": "crowned-heart",
    "Twin Blades": "crossed-sabres", "Whirlwind Slash": "sword-spin", Riposte: "sword-clash", "Focus Training": "targeting",
    "Battle Cry": "hunting-horn", Backstab: "cloak-dagger", Eviscerate: "bleeding-wound", "Deadly Poison": "poison-bottle",
    "Venom Spider": "spider-alt", Evasion: "dodge", Preparation: "hourglass", Smite: "hand-of-god", "Holy Nova": "sun-radiations",
    "Power Word: Shield": "energy-shield", "Divine Blessing": "angel-wings", "Greater Heal": "heart-plus", "Spirit Healer": "angel-outfit",
  };

  const THEMES = [
    { id: "classic", name: "🍺 Taverne", credits: "" },
    {
      id: "grimoire", name: "📜 Grimoire", dir: "assets/grimoire/", ext: ".svg",
      heroes: { Mage: "wizard-face", Tank: "visored-helm", Swordsman: "broadsword", Assassin: "hooded-assassin", Cleric: "sun-priest" },
      cards: GRIMOIRE_CARDS,
      categories: { ATTACK: "crossed-swords", DEFENSE: "shield", RESOURCE: "gems", UTILITY: "magic-potion" },
      credits: "Icônes game-icons.net (Lorc, Delapouite et al.), CC BY 3.0",
    },
    {
      id: "plein-air", name: "🌿 Plein air", dir: "assets/plein-air/", ext: ".png",
      heroes: { Mage: "hero-mage", Tank: "hero-tank", Swordsman: "hero-swordsman", Assassin: "hero-assassin", Cleric: "hero-cleric" },
      cards: {},
      categories: {},
      credits: "Tuiles Kenney Hexagon Pack (kenney.nl), CC0",
    },
  ];

  /** Unknown or removed theme ids (an old saved setting) must still give a working page. */
  function themeById(id) {
    return THEMES.find((t) => t.id === id) || THEMES[0];
  }

  /** Picture of a hero (by class) or a card/minion (by name), as an image path or an emoji. */
  function artFor(themeId, kind, name, category) {
    const theme = themeById(themeId);
    const file = kind === "hero" ? (theme.heroes || {})[name]
      : (theme.cards || {})[name] || (theme.categories || {})[category];
    if (file) return { src: theme.dir + file + theme.ext };
    return { emoji: kind === "hero" ? CLASS_EMOJIS[name] || "🎴" : Arena.Visuals.iconFor(name, category) };
  }

  /** Same picture as HTML, so every view (board, hand, zoom, reveal) draws a theme the same way. */
  function artHtml(themeId, kind, name, category) {
    const art = artFor(themeId, kind, name, category);
    return art.src ? `<img class="art" src="${art.src}" alt="" draggable="false">` : art.emoji;
  }

  Arena.Themes = { THEMES, themeById, artFor, artHtml };
})(globalThis);
