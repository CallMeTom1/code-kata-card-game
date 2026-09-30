# Skirmish Arena

Plain Java 21 + Maven engine that simulates Hearthstone-like card duels between two
bots, and reports the results. No framework, no UI, no database.

## Run it

```bash
mvn test                                   # 168 tests (JUnit 5 + AssertJ)
mvn -q compile exec:java -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank"
mvn -q compile exec:java -Dexec.args="--matches 1 --log --names Alice,Bob"
mvn -q compile exec:java -Dexec.args="--help"
```

| Option | Meaning |
|---|---|
| `--matches N` | number of matches (default 100) |
| `--p1 / --p2 <Bot>:<Class\|auto>` | bots `Aggressive`, `Defensive`, `Random`; classes `Mage`, `Tank`, `Swordsman`, `Assassin`, `Cleric` or `auto` |
| `--seed S` | match *i* uses seed S + *i*; the same arguments always give the same output |
| `--log` | print the first match turn by turn (bracket-tagged, easy to `grep`) |
| `--preset-decks` | use the preset decks of `DESIGN.md` instead of bot-built decks |
| `--names A,B` | player names (default `P1,P2`) |
| `--json FILE` | also write the first match as JSON lines, to replay it in `web/index.html` |
| `--stats-json FILE` | also write the stats of every match as JSON, for the Statistics tab of `web/index.html` |

## Web replay (bonus, outside the brief)

Plain HTML/CSS/JavaScript in [`web/`](web/): no framework, no build, no server, nothing to install.

1. Export a match and/or stats:
   `--matches 1 --json partie.jsonl --names Alice,Bob` and `--matches 1000 --stats-json stats.json --names Alice,Bob`
2. Open `web/index.html` in a browser (double-click), then drag and drop the file(s) onto the page,
   or click **Exemples** to see the bundled sample.
3. Replay: Hearthstone-like board, hands, minions, heroes, mana, statuses, the same log as the console,
   play/pause, step by event or by turn (keys: Space, ←/→, Shift+←/→, Home/End), speed, scrubber.
   Statistics: win split, first-player advantage, match lengths, end reasons, and a table of every match.
4. Front tests: open `web/tests.html` (runs in the browser, no Node needed). With Node:
   `node web/tests/run-node.js`.

## The brief's deliverables

1. **N matches between two bot/deck pairings**: `--matches N --p1 … --p2 …`.
2. **A turn-by-turn log**: `--log`; a full sample is in [`docs/sample-match.log`](docs/sample-match.log).
3. **Aggregate stats**: win rate per side, draws, first-player wins, average length,
   average damage per match, and end reasons, printed after every run.
4. **Design decisions**: [`DESIGN.md`](DESIGN.md), including measured balance.

## Code map

| Package | Role |
|---|---|
| `engine.match` | `Match` (turn loop), `Referee` (end and tie-break), `Bot` / `GameView` / `Action` contracts |
| `engine.cards`, `engine.classes` | the 39 cards, 5 classes, hero powers and preset decks |
| `engine.effects` | reusable effects cards are composed of |
| `engine.combat`, `engine.board`, `engine.player` | armor/Parry/Evasion, minions, champion state and drawing |
| `engine.events` | every state change is a `GameEvent`; the engine never prints |
| `engine.decks` | deck rules and deck-building strategy contract |
| `bots` | Aggressive, Defensive, Random play and deck strategies |
| `log`, `json` | console renderer and JSON exporter (both event listeners), minimal JSON writer |
| `cli`, `stats` | command line, match runner, stats and stats JSON export |
| `web/` | HTML/CSS/JS replay and statistics (reads the JSON exports) |

Team docs: [`CLAUDE.md`](CLAUDE.md), [`docs/TEAM_GUIDELINE.md`](docs/TEAM_GUIDELINE.md),
[`docs/00-PHASE-0-DECISIONS.md`](docs/00-PHASE-0-DECISIONS.md), [`docs/plans/`](docs/plans/).
