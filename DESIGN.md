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
in the order they were played, then minion attacks. Attacks hit the
opponent's armor first, then HP.

## Minions (summons)
Some cards summon minions onto their owner's board (max 5; a summon onto a
full board fizzles). A minion has Attack / Health, e.g. `2/3`.
- Summoning sickness: a minion attacks for the first time in its owner's
  next resolve phase. Minions attack once per turn, oldest first.
- Targeting (no choice, keeps bots simple): an attack — card or minion —
  must hit an enemy **Taunt** minion if there is one, otherwise the enemy
  champion. When a minion hits a minion, both deal their Attack to each
  other. A minion at 0 Health dies immediately.
- Non-Taunt minions can only be damaged by area effects ("each enemy
  minion"). That makes AoE cards (Whirlwind Slash, Holy Nova) valuable.
- Category: a summon is an **Attack** card if the minion is there to deal
  damage, **Defense** if it has Taunt, **Utility** if it has a passive effect.
- Minion damage to the enemy champion counts as damage dealt by its owner.
- Minion hits count as hits: armor absorbs them and Parry reduces them.
  Evasion only stops Attack cards.

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
- **Parry X**: reduces each hit by X, applied after armor.
- **Poison X (N turns)**: opponent loses X HP at the start of each of their
  next N turns. It ignores armor and stacks.
- **Freeze**: the opponent gets 1 less mana on their next turn.
- **Combo**: bonus if another card was already played this turn.
- **Evasion**: the next Attack card that hits you deals 0 damage.
- **Taunt**: enemy attacks must target this minion first.
- **Hero power**: costs 2 mana and can be used once per turn, during the
  play phase.

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
Each class deck = its class cards ×2 + neutral cards to reach 20:
- **Mage** (12 + 8): Mana Crystal×2, Insight×2, Quick Jab×2, Wooden Shield×2
- **Tank** (12 + 8): Iron Wall×2, Strike×2, Crushing Blow×2, Healing Potion×2
- **Épéiste** (10 + 10): Quick Jab×2, Strike×2, Wild Wolf×2, Mana Crystal×2, Wooden Shield×2
- **Assassin** (12 + 8): Quick Jab×2, Wild Wolf×2, Mana Crystal×2, Healing Potion×2
- **Clerc** (12 + 8): Shieldbearer×2, Healing Potion×2, Insight×2, Strike×2

Build order: engine + neutral cards without minions + Mage and Tank spells
first (MVP), then minions, then the other classes.

## Bots
Any bot can play any class.
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

All numbers are a first draft, to rebalance with the `run-simulation` skill.
