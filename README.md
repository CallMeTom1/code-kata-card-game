# Skirmish Arena

A small Hearthstone-like card game engine, built as a code kata. Two champions
(30 HP each), a class deck, hero powers, and a turn loop (draw → mana → play →
resolve → end) simulated by bots. See [`DESIGN.md`](DESIGN.md) for the full
rules and [`CLAUDE.md`](CLAUDE.md) for the project's coding conventions.

## Requirements
- Java 21 (only `JAVA_HOME` needs to point to a JDK 21+; no need to install Maven).
- The repo ships a Maven Wrapper, so you don't need Maven installed globally.

On Windows (PowerShell), always use the wrapper:
```
.\mvnw.cmd <goal>
```
On Linux/macOS:
```
./mvnw <goal>
```
(If you already have `mvn` on your PATH, plain `mvn <goal>` works too.)

## Run the tests
```
.\mvnw.cmd test
```
Runs every unit/integration test (events, `Champion`, `ArmorDamageResolver`,
`TieBreaker`, full `Match` integration).

## Run a batch of matches (the CLI)
The game itself doesn't have a graphical/interactive mode yet: `Main` runs a
*batch* of simulated matches between two bots and prints the result.
```
.\mvnw.cmd -q compile exec:java '-Dexec.args=--matches 5 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 1 --log'
```
> ⚠️ In PowerShell, always wrap the whole `-Dexec.args=...` value in **single
> quotes**, as above. Without it, PowerShell can mis-split the string around
> the `:` characters and Maven will fail with a confusing "Error resolving
> version for plugin '...'" message.

### CLI options (`CommandLineOptions`)
| Flag | Meaning | Values |
|------|---------|--------|
| `--matches N` | how many matches to simulate | any positive integer (default `1`) |
| `--p1 Bot:Class` | player 1's bot and class | bots: `Random`, `Aggressive`, `Defensive`; classes: `Mage`, `Tank` (only these two are implemented so far) |
| `--p2 Bot:Class` | player 2's bot and class | same as above |
| `--seed S` | base RNG seed | any long; match *i* uses `S + i`, so any run is 100% reproducible |
| `--log` | print the full turn-by-turn log | only affects the **first** match of the batch; the rest only feed the aggregate stats |

## Understanding the console output
Running with `--log` prints two parts:

**1. The turn-by-turn log of the first match** — one line per game event
(`ConsoleRenderer`, see `docs/00-PHASE-0-DECISIONS.md` section G1 for the
format decision), for example:
```
=== Match: Alice vs Bob (seed 1) — Alice goes first ===
--- Turn 1 — Alice ---
  Alice draws Fireball
  Alice now has 1 max mana
  Alice plays Frostbolt (2 mana, 0 left)
  Frostbolt hits Bob for 3 (0 absorbed) → Bob 27 HP
--- Turn 1 — Bob ---
  ...
=== Match ended after 8 turns: Alice wins (opponent at 0 HP) ===
```
- `Match: ... — X goes first` — result of the seeded coin flip.
- `Turn N — Player` — one player's half of round `N` (a full "turn" per
  `DESIGN.md` is both players' halves together).
- `draws` / `burns (hand full)` / `takes fatigue damage` — draw phase events.
- `now has N max mana` — mana phase (mana grows by 1 each turn, capped at 10).
- `plays <card> (cost, mana left)` / `uses <hero power>` — play phase.
- `hits <target> for X (Y absorbed) → target Z HP` — resolve phase damage; `Y`
  is how much was stopped by armor/Parry.
- `Match ended after N turns: <winner> (<reason>)` — end of match; `reason` is
  either a champion reaching 0 HP or the 50-turn tie-break (`TieBreaker`).

**2. The aggregate stats**, printed once at the end for the *whole* batch
(`AggregateStats`, computed by `StatsCollector`):
```
Matches: 5
Player1 win rate: 80.0%
Player2 win rate: 20.0%
Draw rate: 0.0%
First player win rate: 60.0%
Average turns: 12.4
Average damage — Player1: 24.6, Player2: 18.2
```
- **win rate** — % of the `--matches` batch won by each side.
- **draw rate** — % ending via tie-break with no HP/damage difference.
- **first player win rate** — % of matches won by whoever won the coin flip
  (sanity check for first-move advantage).
- **average turns** — mean match length in rounds (capped at 50).
- **average damage** — mean HP actually removed from the opponent's champion
  per match, per side (fatigue and minion-vs-minion damage excluded, see
  `DESIGN.md` "Stats & determinism").

### Example sanity checks
Compare a scripted bot against the random baseline — `Aggressive` should win
clearly more often, as required by `DESIGN.md`:
```
.\mvnw.cmd -q compile exec:java '-Dexec.args=--matches 100 --p1 Random:Mage --p2 Random:Tank --seed 1'
.\mvnw.cmd -q compile exec:java '-Dexec.args=--matches 100 --p1 Aggressive:Mage --p2 Random:Tank --seed 1'
```

## Package without repeated compilation (optional)
```
.\mvnw.cmd -q package
java -cp target/classes com.arena.Main --matches 5 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 1 --log
```
