Pokémon Skirmish Arena — Game Design
1. Overview

Pokémon Skirmish Arena is a simplified, Hearthstone-inspired Pokémon card battler.

Two AI-controlled Heroes fight each other using a 20-card deck.

Each Hero has:

30 HP

a unique Hero Power

a hand of cards

a Board containing up to 3 Pokémon

a mana pool

The objective is to reduce the opposing Hero's HP to 0.

The game is fully automated. There is no human player during a match.

The engine is a pure Java/Maven simulation. The frontend is out of scope for the engine.

A future frontend will use PokéAPI for Pokémon artwork, sprites, names and metadata.

2. Heroes

The available Heroes are:

Electhor

Artikodin

Sulfura

Lugia

Ho-Oh

Each Hero starts with 30 HP.

Heroes are permanent characters and remain in play until the match ends.

Each Hero has one unique Hero Power.

Electhor — Static Charge

Cost: 2 mana

Deal 2 damage to the opposing Hero.

Artikodin — Frozen Barrier

Cost: 2 mana

Reduce the next damage received by Artikodin by 4.

The effect expires after blocking damage or at the end of the following turn.

Sulfura — Flame Burst

Cost: 2 mana

Deal 3 damage to the opposing Hero.

If the opposing Hero has 10 HP or less, deal 4 damage instead.

Lugia — Aeroblast

Cost: 2 mana

Deal 2 damage to the opposing Hero and draw 1 card.

Ho-Oh — Sacred Flame

Cost: 2 mana

Heal Ho-Oh for 3 HP.

Its next Attack effect deals +2 damage.

Healing cannot increase HP above 30.

3. Mana

Each Hero has:

maximum mana

available mana

A match starts with 0 maximum mana.

At the beginning of each turn:

Maximum mana increases by 1.

Maximum mana is capped at 10.

Available mana is refilled to maximum mana.

Therefore:

Turn 1 → 1 mana

Turn 2 → 2 mana

Turn 3 → 3 mana

...

Turn 10 → 10 mana

Turn 11+ → 10 mana

Cards and Hero Powers consume available mana.

Resource cards can modify mana but can never increase maximum mana above 10.

Available mana can never become negative.

4. Cards

There are two types of cards:

Pokémon

Items

Cards also belong to one gameplay category:

Attack

Defense

Resource

Utility

These are two separate concepts.

For example:

Pikachu:

Nature: Pokémon

Category: Attack

Potion:

Nature: Item

Category: Utility

The nature describes what the card represents.

The category describes its gameplay purpose.

5. Pokémon

Pokémon are permanent units that occupy space on the Board.

A Pokémon has:

ID

name

mana cost

attack

maximum HP

current HP

owner

gameplay category

optional ability

attack availability state

When a Pokémon is played, it enters the owner's Board.

It remains there until its HP reaches 0.

Unlike Items, Pokémon do not immediately go to the discard pile.

6. Board

Each Hero has a Board with a maximum of 3 Pokémon.

Example:

Electhor — 24 HP

[ Pikachu 2/2 ] [ Onix 2/6 ] [ empty ]

VS

Artikodin — 27 HP

[ Carapuce 1/5 ] [ Bulbizarre 3/4 ] [ empty ]

The Board limit is deliberately small.

This keeps the engine easy to reason about while creating meaningful tactical decisions.

A Hero cannot play another Pokémon while all 3 Board slots are occupied.

7. Summoning

When a Pokémon is played:

The Bot chooses the Pokémon from its hand.

The engine verifies that enough mana is available.

The engine verifies that a Board slot is available.

Mana is deducted.

The Pokémon enters the Board.

Any "when played" ability resolves.

The Pokémon cannot attack during the current turn.

A Pokémon becomes able to attack on its owner's next turn.

This prevents newly played Pokémon from immediately attacking.

8. Pokémon Combat

A Pokémon can attack:

an opposing Pokémon

the opposing Hero

A Pokémon cannot attack:

friendly Pokémon

friendly Heroes

dead Pokémon

a target that no longer exists

A Pokémon can normally attack once per turn.

Pokémon vs Pokémon

Both Pokémon deal damage to each other simultaneously.

Example:

Pikachu: 2 ATK / 2 HP

Carapuce: 1 ATK / 5 HP

Pikachu attacks Carapuce.

Pikachu receives 1 damage → 1 HP.

Carapuce receives 2 damage → 3 HP.

Both remain on the Board.

If a Pokémon reaches 0 HP, it dies.

Pokémon vs Hero

The Pokémon deals its attack value to the opposing Hero.

The Hero does not automatically retaliate.

Example:

Pikachu:
2 ATK

Electhor:
25 HP

Pikachu attacks Electhor.

Electhor becomes 23 HP.

9. Pokémon Death

When a Pokémon reaches 0 HP:

It is removed from the Board.

It is moved to its owner's discard pile.

It can no longer perform actions.

Its temporary effects expire.

Its Board slot becomes available.

HP cannot become negative in the final game state.

10. Pokémon Card Pool

The initial card pool should contain at least these Pokémon.

Pikachu

Cost: 1
Attack: 2
HP: 2

Simple aggressive Pokémon.

Salamèche

Cost: 2
Attack: 3
HP: 3

Efficient offensive Pokémon.

Carapuce

Cost: 2
Attack: 1
HP: 5

Defensive Pokémon with high HP.

Bulbizarre

Cost: 3
Attack: 3
HP: 4

Balanced Pokémon.

Roucool

Cost: 2
Attack: 2
HP: 2

Ability:

The next Attack effect played this turn deals +1 damage.

Reptincel

Cost: 4
Attack: 5
HP: 5

Strong mid-game attacker.

Dracaufeu

Cost: 7
Attack: 8
HP: 8

Powerful late-game Pokémon.

Tortank

Cost: 7
Attack: 6
HP: 10

High-health late-game Pokémon.

Onix

Cost: 3
Attack: 2
HP: 6

Ability:

Reduce the next damage received by its Hero by 2.

Ronflex

Cost: 5
Attack: 3
HP: 9

Very durable defensive Pokémon.

Évoli

Cost: 2
Attack: 2
HP: 3

Ability:

Draw 1 card when played.

Leveinard

Cost: 4
Attack: 1
HP: 7

Ability:

Heal its Hero for 5 HP when played.

Abra

Cost: 3
Attack: 2
HP: 3

Ability:

The next Attack effect played by its Hero deals +3 damage.

11. Item Cards

Items are played from the hand.

They resolve their effect immediately and then enter the discard pile.

Items never occupy Board slots.

Potion

Cost: 2

Heal the Hero for 5 HP.

Super Potion

Cost: 3

Heal the Hero for 8 HP.

Hyper Potion

Cost: 5

Heal the Hero for 12 HP.

Poké Ball

Cost: 2

Draw 2 cards.

Rappel

Cost: 4

Return the highest-cost Pokémon card from the discard pile to the owner's hand.

If there is no Pokémon in the discard pile, nothing happens.

Super Bonbon

Cost: 2

Increase maximum mana by 1 and restore 1 available mana.

Maximum mana remains capped at 10.

Energy

Cost: 1

Restore 2 available mana.

Available mana cannot exceed maximum mana.

Défense X

Cost: 2

Reduce the next damage received by the Hero by 4.

Protection

Cost: 4

Reduce the next damage received by the Hero by 7.

12. Card Identifiers

Every card has a stable technical identifier.

Examples:

PIKACHU
SALAMECHE
CARAPUCE
BULBIZARRE
ROUCOOL
REPTINCEL
DRACAUFEU
TORTANK
ONIX
RONFLEX
EVOLI
LEVEINARD
ABRA

POTION
SUPER_POTION
HYPER_POTION
POKE_BALL
RAPPEL
SUPER_BONBON
ENERGY
DEFENSE_X
PROTECTION

Identifiers must not depend on display names.

This is important for the future frontend and PokéAPI integration.

13. Decks

Every Hero has exactly 20 cards in its deck.

Cards may appear multiple times.

Deck composition is independent from Bot strategy.

The engine should provide several predefined decks.

Aggro

Focuses on:

cheap Pokémon

direct damage

fast Board development

Typical cards:

Pikachu

Salamèche

Roucool

Reptincel

Dracaufeu

Control

Focuses on:

defensive Pokémon

healing

damage reduction

removing opposing Pokémon

Typical cards:

Carapuce

Onix

Ronflex

Potion

Super Potion

Protection

Défense X

Energy

Focuses on:

Resource cards

accelerating mana

expensive Pokémon

Typical cards:

Energy

Super Bonbon

Carapuce

Reptincel

Dracaufeu

Tortank

Balanced

Contains a mixture of all four gameplay categories.

The exact deck lists can be tuned during development.

14. Starting State

At the beginning of a match, each Hero has:

30 HP

0 maximum mana

0 available mana

a shuffled 20-card deck

3 cards in hand

an empty Board

an empty discard pile

The first player is determined using the match's random source.

The random source must be seeded when deterministic execution is required.

15. Turn Structure

Every turn follows:

DRAW
↓
MANA
↓
PLAY
↓
RESOLVE
↓
END

Draw Phase

The active Hero draws one card.

If the deck is empty, nothing happens.

There is no fatigue mechanic in the first version.

Mana Phase

Maximum mana increases by 1, up to 10.

Available mana is refilled.

Play Phase

The Bot may perform multiple actions.

Possible actions include:

play a Pokémon

play an Item

use the Hero Power

attack with an eligible Pokémon

stop playing

The engine validates every action.

Resolve Phase

Pending effects and combat are resolved according to the game rules.

End Phase

Temporary effects are updated or removed.

The turn ends.

16. Action Order

During the Play phase, actions happen sequentially.

Example:

Play Pikachu.

Pikachu enters the Board.

Pikachu cannot attack this turn.

Play Potion.

Hero is healed.

Use Hero Power.

Mana is consumed.

Attack with an existing Pokémon.

Combat resolves.

The Bot chooses the next action after each completed action.

This allows a Bot to react to the updated game state.

17. Hero Powers

A Hero Power:

has a mana cost

can normally be used once per turn

consumes mana

can have offensive, defensive or utility effects

is not part of the deck

Hero Powers should be represented as domain objects rather than special hard-coded branches inside the turn engine.

18. Temporary Effects

Temporary effects must explicitly define:

source

owner

affected target

value

duration

expiration condition

Examples:

Next attack +3 damage

Next incoming damage -5

Damage reduction until end of turn

Avoid unclear boolean flags such as:

isBuffed = true

without a clear expiration rule.

19. Bots

Bots use the Strategy pattern.

The Bot decides what action to take.

The Bot does not directly modify the game state.

The engine remains responsible for:

mana

HP

Board

cards

combat

effects

validation

game progression

AggressiveBot

General priorities:

Take lethal damage when possible.

Remove an opposing Pokémon when that creates significant value.

Develop the strongest affordable Pokémon.

Use damaging Hero Powers.

Attack the opposing Hero when appropriate.

Use useful Items.

Use Resource cards when they enable stronger plays.

The Bot should evaluate both Board control and direct Hero damage.

DefensiveBot

When above 15 HP:

Develop a useful Board.

Remove dangerous opposing Pokémon.

Use efficient defensive cards.

Use Resource cards when useful.

Attack the opposing Hero when safe.

When at 15 HP or below:

Heal.

Reduce incoming damage.

Remove threatening Pokémon.

Use defensive Hero Powers.

Play defensive Pokémon.

Attack when appropriate.

Both Bots must be able to fight any Hero and any deck.

20. Deterministic Simulation

All randomness must be isolated.

The engine must support a seed.

Given the same:

seed

Heroes

decks

Bots

configuration

the simulation should produce the same sequence of events.

This makes failures reproducible.

21. Match End

A match immediately ends when a Hero reaches 0 HP.

The maximum match length is 50 turns.

If both Heroes are alive after turn 50:

The Hero with the highest remaining HP wins.

If HP is equal, compare total damage dealt.

If total damage is also equal, the match is a draw.

This tie-break rule prevents infinite matches while rewarding the Hero that made more progress during the match.

22. Simulation

The application must support running N matches.

The configuration should allow choosing:

Hero 1

Hero 2

Deck 1

Deck 2

Bot 1

Bot 2

random seed

number of matches

Example:

Electhor + AggressiveBot + Aggro Deck

VS

Artikodin + DefensiveBot + Control Deck

The simulation must produce aggregate statistics:

wins for each side

draws

win rate for each side

average match length

average damage dealt per match

23. Match Logging

At least one sample match must produce a human-readable log.

The log should include:

Heroes

starting hands

turn number

current phase

cards drawn

cards played

Pokémon entering the Board

Pokémon attacks

attack targets

Pokémon deaths

Item effects

Hero Power usage

mana changes

damage

healing

defensive effects

Hero HP

final result

Example:

=== Turn 4 ===

Electhor — 24 HP — 4/4 mana

Draw: Potion

Play: Pikachu

Pikachu enters the Board (2 ATK / 2 HP)

Attack: Onix → Salamèche

Onix deals 2 damage.

Salamèche deals 3 damage.

Salamèche is defeated.

End of turn.

The logger must observe the engine state.

It must not contain game rules.

24. PokéAPI and Future Frontend

The future frontend will use PokéAPI for Pokémon data and visuals.

PokéAPI:

https://pokeapi.co/

The engine must remain completely independent from PokéAPI.

The engine should expose stable Pokémon identifiers that the frontend can map to PokéAPI.

Examples:

electhor
artikodin
sulfura
lugia
ho-oh

pikachu
charmander
squirtle
bulbasaur
pidgey
charmeleon
charizard
blastoise
onix
snorlax
eevee
chansey
abra

The future frontend can use these identifiers to retrieve:

sprites

artwork

Pokémon types

names

other presentation data

The engine must not:

make HTTP calls

depend on PokéAPI

parse PokéAPI responses

contain frontend-specific code

25. Architecture

Recommended package structure:

com.pokemonarena

cards

hero

board

deck

game

bot

simulation

logging

Responsibilities:

cards

Card definitions, card categories, Pokémon, Items and effects.

hero

Heroes and Hero Powers.

board

Board state, Pokémon placement and Board operations.

deck

Deck definitions, deck creation and shuffling.

game

Match state, turns, actions, combat, validation and rules.

bot

Bot strategies and action selection.

simulation

Running multiple matches and calculating aggregate statistics.

logging

Human-readable match output.

Avoid generic packages such as:

models

services

managers

helpers

utils

unless there is a concrete reason to introduce one.

26. Design Patterns

Use design patterns only when they make the code easier to understand.

Strategy

Use Strategy for interchangeable Bot behavior.

BotStrategy

AggressiveBot

DefensiveBot

Effects

Represent reusable game effects as small composable objects when useful.

Examples:

DamageEffect

HealEffect

DrawEffect

BuffEffect

DamageReductionEffect

ManaEffect

Do not create an abstraction for every tiny operation if a simple implementation is clearer.

Catalog

A simple Card Catalog and Hero Catalog can provide predefined game content.

Avoid complex factory hierarchies.

Composition

Prefer composition for:

card abilities

Hero Powers

temporary effects

Bot strategies

Avoid deep inheritance hierarchies.

27. Scope

The first version intentionally does not implement:

official Pokémon TCG rules

Pokémon evolution

Pokémon type effectiveness

status conditions

energy attachment

retreat costs

multiple Pokémon type interactions

complex keywords

equipment

multiplayer networking

database persistence

authentication

frontend

PokéAPI integration

animations

tournaments

The goal of the first version is a clean, deterministic and testable automated card battler featuring:

Heroes + Hero Powers + Pokémon Board + Items + Combat + Bots + Simulation.