# Skirmish Arena — Design decisions

Inspired by Hearthstone: each champion picks a **class** that gives a hero
power and a set of class cards, mixed with neutral cards into a 20-card deck.

## Match rules
Each champion starts with 30 HP and a shuffled 20-card deck. A seeded coin
flip decides who goes first. Like in Hearthstone, the first player starts
with 3 cards; the second starts with 4 cards plus **The Coin** (0 mana,
+1 mana this turn only; not part of the deck). Then each player may
**mulligan** once: put back any cards of their opening hand, shuffle them
into the deck and draw the same number (the bot decides which). A "turn" is one full round:
both players take their draw → mana → play → resolve → end phases in order.
The 50-turn limit counts rounds.

Mana: max mana grows by 1 in each mana phase (turn 1 = 1 mana, cap 10), then
refills to max. Unspent mana is lost. Hand limit is 10; extra cards drawn
are burned. Drawing from an empty deck deals fatigue damage (1, then 2,
then 3…), as in Hearthstone. It guarantees long stalemates still end.

Play phase: Resource and draw effects apply immediately, because they
change what can still be played this turn. Everything else, hero power
included, is queued and applied in the resolve phase **in the order it was
played** (as in Hearthstone, where effects resolve in play order), then
minions attack. Damage hits the champion's armor first, then HP.

The match ends **immediately** when a champion reaches 0 HP: the rest of the
queue is dropped and the other player does not play. HP never goes below 0.
If both champions reach 0 HP at the same time, the match is a draw.

## Minions (summons)
Some cards summon minions onto their owner's board (max 7 as in
Hearthstone; a summon onto a full board fizzles). A minion has Attack / Health, e.g. `2/3`.
- Summoning sickness: a minion attacks for the first time in its owner's
  next resolve phase. Minions attack once per turn, oldest first.
- Targeting (no choice, keeps bots simple): a minion attack must hit an
  enemy **Taunt** minion if there is one, otherwise the enemy champion.
  As in Hearthstone, Taunt only stops attacks: Attack cards, poison and
  hero powers ignore it and hit the champion. When a minion hits a minion,
  both deal their Attack to each other. A minion at 0 Health dies immediately.
- Attacking is optional in Hearthstone, so a minion **stays back** instead
  of attacking a Taunt that would kill it without dying itself.
- Cards never target a minion (no targeting choice), so minions are only
  damaged by other minions and by area effects ("each enemy minion"). That
  makes AoE cards (Whirlwind Slash, Holy Nova) valuable.
- Category: a summon is an **Attack** card if the minion is there to deal
  damage, **Defense** if it has Taunt, **Utility** if it has a passive effect.
- Minion damage to the enemy champion counts as damage dealt by its owner.
- Minion hits count as hits: armor absorbs them, Parry reduces them and
  Evasion can prevent one.

We added minions for two reasons: Hearthstone feel, and board
presence, which rewards the Aggressive bot for playing early and forces
Defensive to value Taunt.

## Tie-break (after 50 turns)
1. Higher remaining HP wins — it best reflects who was ahead.
2. If HP is equal, whoever dealt more total damage wins — rewards the
   player who pushed the game.
3. If still equal, the match is a draw (counted separately in stats).

## Keywords
- **Armor X (N turns)**: absorbs X damage; removed at the start of its
  owner's Nth next turn. Several armors stack.
- **Parry X (N turns)**: reduces each hit by X, applied after armor; expires
  like armor.
- **Poison X (N turns)**: opponent loses X HP at the start of each of their
  next N turns. It ignores armor and stacks.
- **Freeze**: the opponent gets 1 less mana on their next turn.
- **Combo**: bonus if another card was already played this turn (The Coin
  counts, the hero power does not — as in Hearthstone).
- **Evasion**: works like a Hearthstone Secret. The next damage the enemy
  deals to your champion (card, hit of a minion or hero power) is prevented.
  Poison ticks and fatigue are not prevented.
- **Next Attack +X** (Sharpen, Battle Cry, Focus Training): like Hearthstone
  Spell Damage, it adds X to **each** damage instance of the next Attack
  card that deals damage (each hit of Twin Blades, each target of an area
  effect). Summon and poison cards do not use it. Several buffs stack and
  are all used by that card.
- **Taunt**: enemy attacks must target this minion first.
- **Hero power**: costs 2 mana and can be used once per turn, during the
  play phase. It resolves like a card, in play order.
- **Mana**: max mana and available mana are both capped at 10, as in
  Hearthstone. The Coin or Preparation at 10 mana give nothing more.
- **Shield Slam** counts the armor the Tank has when it resolves, including
  armor played earlier in the same turn: play order matters.

## Neutral cards (10)
| Category | Card           | Cost | Effect                          |
|----------|----------------|------|---------------------------------|
| Attack   | Quick Jab      | 1    | 2 damage                        |
| Attack   | Strike         | 2    | 4 damage                        |
| Attack   | Crushing Blow  | 5    | 8 damage                        |
| Attack   | Wild Wolf      | 2    | Summon a 2/2 Wolf               |
| Defense  | Wooden Shield  | 1    | Armor 2 (2 turns)               |
| Defense  | Iron Wall      | 3    | Armor 5 (2 turns)               |
| Defense  | Shieldbearer   | 1    | Summon a 0/3 Shieldbearer, Taunt|
| Resource | Mana Crystal   | 1    | +1 max mana permanently         |
| Utility  | Insight        | 1    | Draw 1 card                     |
| Utility  | Healing Potion | 2    | Heal 4 HP (max 30)              |

## Classes
**Mage** — burst spells and necromancy. Hero power *Fireblast*: deal 1 damage.
| Cat. | Card             | Cost | Effect                             |
|------|------------------|------|------------------------------------|
| A    | Frostbolt        | 2    | 3 damage, Freeze                   |
| A    | Fireball         | 4    | 6 damage                           |
| A    | Pyroblast        | 8    | 10 damage                          |
| A    | Raise Skeletons  | 3    | Summon two 1/1 Skeletons           |
| D    | Ice Barrier      | 3    | Armor 6 (2 turns)                  |
| U    | Arcane Intellect | 3    | Draw 2 cards                       |

**Tank** — armor and endurance. Hero power *Armor Up*: Armor 2 (3 turns).
| Cat. | Card        | Cost | Effect                                   |
|------|-------------|------|------------------------------------------|
| A    | Shield Slam | 1    | Damage equal to your current armor       |
| D    | Shield Block| 3    | Armor 4 (2 turns), draw 1 card           |
| D    | Fortress    | 5    | Armor 8 (2 turns)                        |
| D    | Iron Golem  | 4    | Summon a 0/6 Iron Golem, Taunt           |
| R    | War Chest   | 2    | +1 max mana, Armor 2 (2 turns)           |
| U    | Last Stand  | 4    | Heal 6 HP                                |

**Épéiste (`Swordsman` on the command line)** — multi-hit, buffs and minion clearing (no summons:
a lone duellist). Hero power *Sharpen*: your next Attack card deals +2 damage.
| Cat. | Card             | Cost | Effect                                           |
|------|------------------|------|--------------------------------------------------|
| A    | Twin Blades      | 3    | 2 hits of 3 damage                               |
| A    | Whirlwind Slash  | 5    | 3 damage to the enemy champion and each enemy minion |
| D    | Riposte          | 2    | Parry 2 (1 turn), deal 2 damage                  |
| R    | Focus Training   | 1    | +1 max mana, next Attack +1                      |
| U    | Battle Cry       | 1    | Next Attack card deals +3 damage                 |

**Assassin** — cheap cards, combos and poison. Hero power *Poisoned Dagger*:
Poison 1 (2 turns).
| Cat. | Card          | Cost | Effect                                        |
|------|---------------|------|-----------------------------------------------|
| A    | Backstab      | 0    | 2 damage                                      |
| A    | Eviscerate    | 3    | 3 damage ignoring armor; Combo: 5             |
| A    | Deadly Poison | 2    | Poison 2 (3 turns)                            |
| A    | Venom Spider  | 2    | Summon a 1/2 Spider; its hits on the champion also apply Poison 1 (2 turns) |
| D    | Evasion       | 2    | Evasion (lasts up to 2 turns)                 |
| R    | Preparation   | 0    | +2 mana this turn only                        |

**Clerc (`Cleric` on the command line)** — healing and attrition. Hero power *Lesser Heal*: heal
2 HP.
| Cat. | Card                | Cost | Effect                                              |
|------|---------------------|------|-----------------------------------------------------|
| A    | Smite               | 1    | 2 damage                                            |
| A    | Holy Nova           | 5    | 2 damage to the enemy champion and each enemy minion, heal 2 HP |
| D    | Power Word: Shield  | 1    | Armor 3 (2 turns), draw 1 card                      |
| R    | Divine Blessing     | 2    | +1 max mana, heal 2 HP                              |
| U    | Greater Heal        | 3    | Heal 6 HP                                           |
| U    | Spirit Healer       | 3    | Summon a 0/3 Spirit with Taunt; heals you 2 HP at the end of each of your turns |

Twin Blades hits armor hard but is weak against Parry. Eviscerate and Poison
get through Tank armor. Whirlwind Slash and Holy Nova are the answers to
Skeletons, Wolves and Spiders. Taunt minions slow down aggressive decks.
These counters are on purpose, so class matchups actually differ.

## Decks (20 cards)
Deck rules: exactly 20 cards, at most 2 copies of a card, at least 6 class
cards, only the cards of the chosen class plus neutral cards. The engine
checks these rules and rejects an invalid deck before the match starts.

Decks come from two sources:
- **Preset** decks (below): stable reference decks, used for balancing.
- **Built by the bot**: each bot has a `DeckStrategy` (separate from its
  play strategy — Interface Segregation) that picks a class and builds a
  deck from the card pool. With `--p1 Aggressive:auto` the bot chooses its
  class too; with `--p1 Aggressive:Mage` the class is imposed. Add
  `--preset-decks` to use the presets instead.

Preset decks = the class cards ×2 + neutral cards to reach 20:
- **Mage** (12 + 8): Mana Crystal×2, Insight×2, Quick Jab×2, Wooden Shield×2
- **Tank** (12 + 8): Iron Wall×2, Strike×2, Crushing Blow×2, Healing Potion×2
- **Épéiste** (10 + 10): Quick Jab×2, Strike×2, Wild Wolf×2, Mana Crystal×2, Wooden Shield×2
- **Assassin** (12 + 8): Quick Jab×2, Wild Wolf×2, Mana Crystal×2, Healing Potion×2
- **Clerc** (12 + 8): Shieldbearer×2, Healing Potion×2, Insight×2, Strike×2

Build order: engine + neutral cards without minions + Mage and Tank spells
first (MVP), then minions, then the other classes.

## Bots
Any bot can play any class. Each bot = a play strategy + a deck strategy.

Deck strategies:
- **Aggressive**: picks the class with the most damage per mana (default:
  Assassin), then fills the deck with the cheapest Attack cards and
  attacking minions first, Resource cards second, no heals.
- **Defensive**: picks the class with the most armor and healing (default:
  Tank or Clerc), then Defense, heals and Taunt minions first, and at
  least 6 Attack cards so it can still win.
- **Random**: random class, random legal deck (seeded).

With `auto`, the class is fixed per bot so matches stay readable:
Aggressive → Assassin, Defensive → Tank, Random → random.

Mulligan: Aggressive puts back cards costing 4 or more, Defensive cards
costing 5 or more, Random a random subset.

Play strategies:
- **Aggressive**: plays Resource cards first, then the highest-damage
  affordable Attack, repeating while mana allows. An attacking minion is
  valued at twice its Attack, because it hits every turn. It uses buffs
  only if an Attack can follow, and the hero power with leftover mana.
- **Defensive**: above 15 HP it behaves like Aggressive. At 15 HP or below
  it plays heals, Taunt minions and the largest Defense first, and attacks
  only with the mana left over.
- **Random** (baseline): plays random affordable cards. A sanity check:
  both real bots should beat it clearly.

## Stats & determinism
Every match uses its own seed (`baseSeed + matchIndex`), so any match in a
batch can be replayed with its exact log (`--seed <that seed> --matches 1 --log`).
The seed is mixed (`Seeds.random`) before creating `java.util.Random`: with raw
consecutive seeds, the first coin flip was identical and player 1 started all
1000 matches. "Damage dealt" is the HP the
opponent's champion actually lost to your cards, minions, poison and hero
power. Damage to minions and fatigue are excluded. Aggregates report the
win rate per side, the draw rate, the first-player win rate, the average
match length in turns, and the average damage dealt per match per side.

## Logging
The engine never prints. Every state change publishes an immutable
`GameEvent` (turn started, card played, damage dealt, minion summoned…)
that carries everything needed to display it. The console log, the stats
and a future JSON export for an HTML/JS replay are all listeners. We chose
this so we can add a front without touching the engine, and so that
"everything is visible" can be checked by tests. `--log` prints the first
match in full; the other matches only feed the stats.

Console format: one line per event, starting with bracket tags
`[turn][player][EVENT  ]` of fixed width, so the log is easy to read and to
filter with `grep`. Before turn 1, a **setup block** shows each side's class,
hero power, deck (grouped by category) and mana curve, so a reader knows
what each bot chose before the fight starts:
```
[MATCH  ] Alice [Aggressive:Mage] vs Bob [Defensive:Tank] | seed 42
[SETUP  ][Alice] [Aggressive] plays [Mage] (imposed) | deck: built
[POWER  ][Alice] Fireblast (2) — deal 1 damage
[DECK   ][Alice] [ATTACK   16] Fireball x2, Pyroblast x2, Frostbolt x2, Strike x2, Wild Wolf x2, Crushing Blow x2, Quick Jab x2, Raise Skeletons x2
[DECK   ][Alice] [RESOURCE  2] Mana Crystal x2
[DECK   ][Alice] [UTILITY   2] Insight x2
[CURVE  ][Alice] [1: 6] [2: 6] [3: 2] [4: 2] [5+: 4] | avg cost 2.9
[SETUP  ][Bob  ] [Defensive] plays [Tank] (imposed) | deck: built
[POWER  ][Bob  ] Armor Up (2) — Armor 2 (3 turns)
[DECK   ][Bob  ] [ATTACK    6] Crushing Blow x2, Strike x2, Wild Wolf x2
[DECK   ][Bob  ] [DEFENSE  10] Fortress x2, Iron Golem x2, Iron Wall x2, Shield Block x2, Shieldbearer x2
[DECK   ][Bob  ] [UTILITY   4] Last Stand x2, Healing Potion x2
[CURVE  ][Bob  ] [1: 2] [2: 6] [3: 4] [4: 4] [5+: 4] | avg cost 3.1
[START  ] Bob goes first (coin flip) | Alice gets The Coin
[SWAP   ][Bob  ] keeps the whole hand
[SWAP   ][Alice] puts back Crushing Blow → draws Crushing Blow
[HAND   ][Bob  ] Iron Wall, Iron Wall, Last Stand
[HAND   ][Alice] Strike, Frostbolt, Raise Skeletons, Crushing Blow, The Coin
[T01] ======================================================================
[T01][Bob  ][TURN   ] [HP 30/30] [ARMOR 0] [HAND 3] [DECK 17]
[T01][Bob  ][DRAW   ] Strike
[T01][Bob  ][MANA   ] [MANA 1/1]
[T01][Alice][TURN   ] [HP 30/30] [ARMOR 0] [HAND 5] [DECK 16]
[T01][Alice][DRAW   ] Pyroblast
```
The full list of tags and a longer example are in
`docs/00-PHASE-0-DECISIONS.md` (section G1).

## Web replay (bonus)
The brief asks for no UI, so the front is a separate, read-only bonus: `web/` is a
static HTML/CSS/JS page that replays the JSON exports (`--json` for one match,
`--stats-json` for a batch). The engine did not change for it, apart from one
field: `DamageDealt` now carries the armor left after the hit. The JS log formatter
reproduces the console lines exactly; a test compares it with `docs/sample-match.log`.
We chose plain files over a web server or a framework because the team's work PCs
cannot install Node, and because opening a file needs no setup.
Every card also has a rules text (`Card.text`, exported in `PlayerSetUp`) so the replay can
show it. Sounds and the background music are synthesized (Web Audio): the music is one of
four original loops (`MUSIC_THEMES` in `sound.js`: epic in D minor, tavern, boss, mystic)
whose layers follow the match (setup, battle, climax at 10 HP or less, silence at the end).
Animations use the Web Animations API, no library.
The game fills the window under the top bar (it only scrolls when the window is too small,
and gets denser under 900 px high); the HP chart is a folded section below it.
Design themes (`themes.js`) are a palette in `arena.css` plus optional pictures per hero class
and card; a missing picture falls back to the original emoji, so the classic look is never lost
and a theme can be added one picture at a time. Their pictures are downloaded once and
committed under `web/assets/` (the page must work offline from a file): SVG icons from
game-icons.net (CC BY 3.0, credited in the footer) and Kenney tiles (CC0, PNG, plus one JPG
board built from them). Sources and licenses: `web/assets/CREDITS.md`. Both only run when the replay moves one event forward, get
shorter at high speed, and animations are skipped when the OS asks for reduced motion.

## LLM bots (Claude API)
Two AIs can duel: `--p1 Llm:auto --p2 Llm:auto`. An `Llm` player is two classes behind the
`LlmClient` interface (faked in tests, `ClaudeLlmClient` in `Main`):
- **`LlmDeckStrategy`** asks the model for its class and 20 cards. `DeckValidator` stays the
  judge: an illegal deck gets one more try with the errors, then the Aggressive deck is used.
- **`LlmBot`** makes **one call per turn**: the model returns an ordered plan (`PLAY <card>`,
  `HERO_POWER`) that the bot plays step by step (the engine still asks one action at a time).
  A step that is not legal any more, or an API error, hands the rest of the turn to the Aggressive
  bot, so a model mistake never stalls a match. The mulligan is also asked to the model.
- Answers are **structured outputs** (a JSON schema per question), so they always parse. Each
  answer has a private `thought` and a `message` said aloud to the opponent (in French).
- **Talking**: bots stay free of IO; `Bot.takeSpeech()` (silent by default) hands the words to the
  engine, which publishes a `BotSpoke` event (`[THINK  ]` and `[SAY    ]` log lines, speech bubbles and
  a Dialogue panel in the web replay). The opponent reads the last message in
  `GameView.opponentLastWords()`, so the two AIs answer each other.
- **Richer `GameView`** (team decision): both class names, own max mana and deck size, and the
  minions of both boards (`MinionView`), because a model plays blind without them.
- **Dependency** (team decision): the official `com.anthropic:anthropic-java` SDK is the only
  runtime dependency; nothing outside `bots.llm` and `Main` sees it.
- **Settings**: the key is read from `ANTHROPIC_API_KEY` (environment), then `.env.local`
  (git-ignored), then `.env` (committed, placeholder only). Model: `--llm-model`, then
  `ARENA_LLM_MODEL` (the committed `.env` sets `claude-haiku-4-5`: cheapest and fastest for tests),
  then `claude-opus-5-5`. Models that accept it run at `low` effort, and a refused request falls back
  server-side to another model; Haiku 4.5 rejects both options, so they are only sent to newer models. The rules and card catalog are one cached system prompt.
- About 25 calls per match (2 decks, 2 mulligans, ~20 turns): run a few matches, not 1000.
  LLM matches are not replayable from the seed.

## Live server (`--serve`)
Starting a match from the page and watching it while it is played (LLM turns take seconds)
needs a server; the brief asks for no UI, so it is a small optional extra, decided with the team:
- `ArenaServer` uses the JDK `com.sun.net.httpserver.HttpServer` (no framework, no new dependency)
  and listens on `localhost` only. It serves `web/`, `GET /api/options` (bots, classes, whether an
  API key is set), `POST /api/matches` (`{"p1":"Llm:auto","p2":"Defensive:Tank","names":[..]}`) and
  `GET /api/matches/{id}/events`, a Server-Sent Events stream.
- The stream is just another `GameEventListener` (`LiveMatch`): it keeps every event as the same JSON
  line as `--json`, so a browser connecting late still gets the whole match. The engine did not change.
- The page shows **Nouvelle partie** only when served over http; opened as a file it stays the replay
  viewer. The replay extends its timeline event by event and keeps playing at the chosen speed,
  waiting at the end while the match is live.

## Deliberate differences from Hearthstone
We follow Hearthstone wherever the brief allows. The exceptions:
- **20-card decks** (not 30), **armor that expires after N turns** (not
  permanent) and **a resolve phase**: all three are required by the brief.
- **No targeting**: cards hit the enemy champion and minions attack
  automatically. That keeps the bots simple and every match reproducible.
- **Freeze** removes 1 mana from the opponent's next turn (there is nothing
  to freeze, since champions do not attack). **Parry** and **Poison** are our
  own keywords.
- Players are named `P1` and `P2` by default (`--names Alice,Bob` to change).

## Balance (measured)
The first numbers were a draft; 1000-match runs showed Defensive:Tank winning
96% against Aggressive:Mage. Armor and heals gave more value per mana than
attacks, and two minions were untouchable value engines, because cards never
target minions (Iron Golem 2/6 Taunt, which enemy minions refused to attack,
and Spirit Healer without Taunt). Fixes: lower armor and heals, Holy Nova at
its Hearthstone values, Iron Golem 0/6, Spirit Healer with Taunt, minions
that do not suicide into a Taunt, and smarter Aggressive decisions.

Reference results (seed 42, 1000 matches, bot-built decks):

| Matchup | P1 win rate | First player wins | Avg length |
|---|---|---|---|
| Aggressive:Mage vs Defensive:Tank (default) | 46.7% | 50.3% | 9.6 turns |
| Aggressive:auto vs Defensive:auto (Assassin vs Tank) | 46.4% | 52.2% | 9.6 turns |
| Aggressive:Mage vs Random:Mage | 91.2% | 51.0% | 7.1 turns |
| Defensive:Tank vs Random:Tank | 90.6% | 52.6% | 19.6 turns |
| Random:auto vs Random:auto | 50.1% | 51.1% | 13.3 turns |

Known outliers, left for the team to tune: a class played against its
nature. Aggressive:Tank and Aggressive:Cleric build decks with almost no
armor or heals, and lose nearly every match (0-2%) against Defensive:Tank or
Defensive:Cleric. Cleric also beats Tank 94% when both are Aggressive. The
`run-simulation` skill is the tool to keep tuning; every change of a number
goes in this file.
