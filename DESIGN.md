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
| Defense  | Wooden Shield  | 1    | Armor 3 (2 turns)               |
| Defense  | Iron Wall      | 3    | Armor 7 (2 turns)               |
| Defense  | Shieldbearer   | 1    | Summon a 0/3 Shieldbearer, Taunt|
| Resource | Mana Crystal   | 1    | +1 max mana permanently         |
| Utility  | Insight        | 1    | Draw 1 card                     |
| Utility  | Healing Potion | 2    | Heal 5 HP (max 30)              |

## Classes
**Mage** — burst spells and necromancy. Hero power *Fireblast*: deal 1 damage.
| Cat. | Card             | Cost | Effect                             |
|------|------------------|------|------------------------------------|
| A    | Frostbolt        | 2    | 3 damage, Freeze                   |
| A    | Fireball         | 4    | 6 damage                           |
| A    | Pyroblast        | 8    | 10 damage                          |
| A    | Raise Skeletons  | 3    | Summon two 1/1 Skeletons           |
| D    | Ice Barrier      | 3    | Armor 8 (2 turns)                  |
| U    | Arcane Intellect | 3    | Draw 2 cards                       |

**Tank** — armor and endurance. Hero power *Armor Up*: Armor 2 (3 turns).
| Cat. | Card        | Cost | Effect                                   |
|------|-------------|------|------------------------------------------|
| A    | Shield Slam | 1    | Damage equal to your current armor       |
| D    | Shield Block| 3    | Armor 5 (2 turns), draw 1 card           |
| D    | Fortress    | 5    | Armor 12 (3 turns)                       |
| D    | Iron Golem  | 4    | Summon a 2/6 Iron Golem, Taunt           |
| R    | War Chest   | 2    | +1 max mana, Armor 2 (2 turns)           |
| U    | Last Stand  | 4    | Heal 8 HP                                |

**Épéiste (Swordsman)** — multi-hit, buffs and minion clearing (no summons:
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

**Clerc (Cleric)** — healing and attrition. Hero power *Lesser Heal*: heal
2 HP.
| Cat. | Card                | Cost | Effect                                              |
|------|---------------------|------|-----------------------------------------------------|
| A    | Smite               | 1    | 2 damage                                            |
| A    | Holy Nova           | 4    | 2 damage to the enemy champion and each enemy minion, heal 3 HP |
| D    | Power Word: Shield  | 1    | Armor 3 (2 turns), draw 1 card                      |
| R    | Divine Blessing     | 2    | +1 max mana, heal 2 HP                              |
| U    | Greater Heal        | 3    | Heal 7 HP                                           |
| U    | Spirit Healer       | 3    | Summon a 0/3 Spirit; heals you 2 HP at the end of each of your turns |

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
batch can be replayed with its exact log. "Damage dealt" is the HP the
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
[SETUP  ][Alice] [Aggressive] plays [Mage] (imposed) | deck: preset
[POWER  ][Alice] Fireblast (2) — deal 1 damage
[DECK   ][Alice] [ATTACK   10] Quick Jab x2, Frostbolt x2, Fireball x2, Raise Skeletons x2, Pyroblast x2
[DECK   ][Alice] [DEFENSE   4] Wooden Shield x2, Ice Barrier x2
[DECK   ][Alice] [RESOURCE  2] Mana Crystal x2
[DECK   ][Alice] [UTILITY   4] Insight x2, Arcane Intellect x2
[CURVE  ][Alice] [1: 8] [2: 2] [3: 6] [4: 2] [5+: 2] | avg cost 2.7
[SETUP  ][Bob  ] ...
[START  ] Alice goes first (coin flip) | Bob gets The Coin
[T01] ======================================================================
[T01][Alice][TURN   ] [HP 30/30] [ARMOR 0] [MANA 1/1] [HAND 3] [DECK 17]
```
The full list of tags and a longer example are in
`docs/00-PHASE-0-DECISIONS.md` (section G1).

## Later: LLM bots
Once the required deliverables are done, an `LlmBot` (another `Bot`
implementation, behind an `LlmClient` interface faked in tests) could let
real LLMs duel. Constraints already agreed: a few matches only (API cost),
one call per turn, a structured JSON answer with a `reason` shown as a
`[THINK  ]` log line, fallback to a classic bot on illegal moves or API
errors, API key only in the `ANTHROPIC_API_KEY` environment variable.
LLM matches are not replayable from the seed.

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

All numbers are a first draft, to rebalance with the `run-simulation` skill.
