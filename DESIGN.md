# Skirmish Arena — Design decisions

Inspired by Hearthstone: each champion picks a **class** that gives a hero
power and a set of class cards, mixed with neutral cards into a 20-card deck.

## Match rules
Each champion starts with 30 HP and a shuffled 20-card deck. A seeded coin
flip decides who goes first. Like in Hearthstone, the first player starts
with 3 cards; the second starts with 4 cards plus **The Coin** (0 mana,
+1 mana this turn only; not part of the deck). A "turn" is one full round:
both players take their draw → mana → play → resolve → end phases in order.
The 50-turn limit counts rounds.

Mana: max mana grows by 1 in each mana phase (turn 1 = 1 mana, cap 10), then
refills to max. Unspent mana is lost. Hand limit is 10; extra cards drawn
are burned. Drawing from an empty deck deals fatigue damage (1, then 2,
then 3…), as in Hearthstone. It guarantees long stalemates still end.

Play phase: Resource and draw effects apply immediately, because they
change what can still be played this turn. Everything else is queued and
applied in the resolve phase: Defense and heal effects first, then Attacks
in the order they were played. Attacks hit the opponent's armor first, then HP.

## Tie-break (after 50 turns)
1. Higher remaining HP wins — it best reflects who was ahead.
2. If HP is equal, whoever dealt more total damage wins — rewards the
   player who pushed the game.
3. If still equal, the match is a draw (counted separately in stats).

## Keywords
- **Armor X (N turns)**: absorbs X damage; removed at the start of its
  owner's Nth next turn. Several armors stack.
- **Parry X**: reduces each hit by X, applied after armor.
- **Poison X (N turns)**: opponent loses X HP at the start of each of their
  next N turns. It ignores armor and stacks.
- **Freeze**: the opponent gets 1 less mana on their next turn.
- **Combo**: bonus if another card was already played this turn.
- **Evasion**: the next Attack card that hits you deals 0 damage.
- **Hero power**: costs 2 mana and can be used once per turn, during the
  play phase.

## Neutral cards (8)
| Category | Card           | Cost | Effect                          |
|----------|----------------|------|---------------------------------|
| Attack   | Quick Jab      | 1    | 2 damage                        |
| Attack   | Strike         | 2    | 4 damage                        |
| Attack   | Crushing Blow  | 5    | 8 damage                        |
| Defense  | Wooden Shield  | 1    | Armor 3 (2 turns)               |
| Defense  | Iron Wall      | 3    | Armor 7 (2 turns)               |
| Resource | Mana Crystal   | 1    | +1 max mana permanently         |
| Utility  | Insight        | 1    | Draw 1 card                     |
| Utility  | Healing Potion | 2    | Heal 5 HP (max 30)              |

## Classes (5 class cards each)
**Mage** — burst spells. Hero power *Fireblast*: deal 1 damage.
| Cat. | Card             | Cost | Effect                             |
|------|------------------|------|------------------------------------|
| A    | Frostbolt        | 2    | 3 damage, Freeze                   |
| A    | Fireball         | 4    | 6 damage                           |
| A    | Pyroblast        | 8    | 10 damage                          |
| D    | Ice Barrier      | 3    | Armor 8 (2 turns)                  |
| U    | Arcane Intellect | 3    | Draw 2 cards                       |

**Tank** — armor and endurance. Hero power *Armor Up*: Armor 2 (3 turns).
| Cat. | Card        | Cost | Effect                                   |
|------|-------------|------|------------------------------------------|
| A    | Shield Slam | 1    | Damage equal to your current armor       |
| D    | Shield Block| 3    | Armor 5 (2 turns), draw 1 card           |
| D    | Fortress    | 5    | Armor 12 (3 turns)                       |
| R    | War Chest   | 2    | +1 max mana, Armor 2 (2 turns)           |
| U    | Last Stand  | 4    | Heal 8 HP                                |

**Épéiste (Swordsman)** — multi-hit and buffs. Hero power *Sharpen*: your
next Attack card deals +2 damage.
| Cat. | Card             | Cost | Effect                            |
|------|------------------|------|-----------------------------------|
| A    | Twin Blades      | 3    | 2 hits of 3 damage                |
| A    | Whirlwind Slash  | 5    | 3 hits of 3 damage                |
| D    | Riposte          | 2    | Parry 2 (1 turn), deal 2 damage   |
| R    | Focus Training   | 1    | +1 max mana, next Attack +1       |
| U    | Battle Cry       | 1    | Next Attack card deals +3 damage  |

**Assassin** — cheap cards, combos and poison. Hero power *Poisoned Dagger*:
Poison 1 (2 turns).
| Cat. | Card          | Cost | Effect                                  |
|------|---------------|------|-----------------------------------------|
| A    | Backstab      | 0    | 2 damage                                |
| A    | Eviscerate    | 3    | 3 damage ignoring armor; Combo: 5       |
| A    | Deadly Poison | 2    | Poison 2 (3 turns)                      |
| D    | Evasion       | 2    | Evasion (lasts up to 2 turns)           |
| R    | Preparation   | 0    | +2 mana this turn only                  |

**Clerc (Cleric)** — healing and attrition. Hero power *Lesser Heal*: heal
2 HP.
| Cat. | Card                | Cost | Effect                         |
|------|---------------------|------|--------------------------------|
| A    | Smite               | 1    | 2 damage                       |
| A    | Holy Nova           | 4    | 3 damage, heal 3 HP            |
| D    | Power Word: Shield  | 1    | Armor 3 (2 turns), draw 1 card |
| R    | Divine Blessing     | 2    | +1 max mana, heal 2 HP         |
| U    | Greater Heal        | 3    | Heal 7 HP                      |

Twin Blades and Whirlwind Slash hit armor hard but are weak against Parry.
Eviscerate and Poison get through Tank armor. These counters are on purpose,
so class matchups actually differ.

## Decks (20 cards)
Each class deck = its 5 class cards ×2 + 10 neutral cards:
- **Mage**: Mana Crystal×2, Insight×2, Quick Jab×2, Strike×2, Wooden Shield×2
- **Tank**: Iron Wall×2, Wooden Shield×2, Strike×2, Crushing Blow×2, Healing Potion×2
- **Épéiste**: Quick Jab×2, Strike×2, Crushing Blow×2, Mana Crystal×2, Wooden Shield×2
- **Assassin**: Quick Jab×2, Strike×2, Insight×2, Mana Crystal×2, Healing Potion×2
- **Clerc**: Healing Potion×2, Iron Wall×2, Insight×2, Strike×2, Mana Crystal×2

Build order: engine + neutral cards + Mage and Tank first (MVP), then the
other classes.

## Bots
Any bot can play any class.
- **Aggressive**: plays Resource cards first, then the highest-damage
  affordable Attack, repeating while mana allows; uses buffs only if an
  Attack can follow; uses the hero power with leftover mana.
- **Defensive**: above 15 HP it behaves like Aggressive. At 15 HP or below
  it plays heals and the largest Defense first, and attacks only with the
  mana left over.
- **Random** (baseline): plays random affordable cards. A sanity check:
  both real bots should beat it clearly.

## Stats & determinism
Every match uses its own seed (`baseSeed + matchIndex`), so any match in a
batch can be replayed with its exact log. "Damage dealt" is the HP the
opponent actually lost to your cards, poison and hero power. It excludes
fatigue. Aggregates report the win rate per side, the draw rate, the
first-player win rate, the average match length in turns, and the average
damage dealt per match per side.

All numbers are a first draft, to rebalance with the `run-simulation` skill.
