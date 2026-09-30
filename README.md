# Skirmish Arena

Plain Java 21 + Maven engine that simulates Hearthstone-like card duels between two
bots, and reports the results. No framework, no UI, no database.

## Run it

```bash
mvn test                                   # 171 tests (JUnit 5 + AssertJ)
mvn -q compile exec:java -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank"
mvn -q compile exec:java -Dexec.args="--matches 1 --log --names Alice,Bob"
mvn -q compile exec:java -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 42 --names Alice,Bob --json partie.jsonl --stats-json stats.json"
mvn -q compile exec:java -Dexec.args="--help"
```

| Option | Meaning |
|---|---|
| `--matches N` | number of matches (default 100) |
| `--p1 / --p2 <Bot>:<Class\|auto>` | bots `Aggressive`, `Defensive`, `Random`, `Llm` (Claude API); classes `Mage`, `Tank`, `Swordsman`, `Assassin`, `Cleric` or `auto` |
| `--seed S` | match *i* uses seed S + *i*; the same arguments always give the same output |
| `--log` | print the first match turn by turn (bracket-tagged, easy to `grep`) |
| `--preset-decks` | use the preset decks of `DESIGN.md` instead of bot-built decks |
| `--names A,B` | player names (default `P1,P2`) |
| `--json FILE` | also write the first match as JSON lines, to replay it in `web/index.html` |
| `--llm-model ID` | model of the `Llm` bots (default `ARENA_LLM_MODEL`, else `claude-opus-5-5`) |
| `--stats-json FILE` | also write the stats of every match as JSON, for the Statistics tab of `web/index.html` |

## Two AIs duelling (Claude API)

The `Llm` bot lets Claude choose its class, its deck, its mulligan and every turn, and talk to its opponent.

1. Put your key in `.env.local` at the project root (git-ignored; `.env` only holds the placeholder):
   `ANTHROPIC_API_KEY=sk-ant-...`
2. Run a match and export it for the web replay:

   ```bash
   mvn -q compile exec:java -Dexec.args="--matches 1 --p1 Llm:auto --p2 Llm:auto --names Claude-A,Claude-B --log --json duel.jsonl"
   ```

3. Drop `duel.jsonl` onto `web/index.html`: speech bubbles, a **Dialogue** panel and the `[THINK]`/`[SAY]` lines
   show what each AI thinks and says. An AI can also face a classic bot (`--p2 Defensive:Tank`).

About 25 API calls per match: keep `--matches` small. Details and fallbacks: `DESIGN.md` → "LLM bots".

## Web replay (bonus, outside the brief)

Plain HTML/CSS/JavaScript in [`web/`](web/): no framework, no build, no server, nothing to install.

1. Export the files the front reads, in a single run (from the project root):

   ```bash
   mvn -q compile exec:java -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 42 --names Alice,Bob --json partie.jsonl --stats-json stats.json"
   ```

   | File | Content | Used by |
   |---|---|---|
   | `partie.jsonl` (`--json`) | the **first** match, one `GameEvent` per line | **Replay** tab |
   | `stats.json` (`--stats-json`) | summary + one row per match of the run | **Statistics** tab |

   The console confirms with `[EXPORT ] first match written to partie.jsonl` and
   `[EXPORT ] stats written to stats.json`. Both options are independent: use only `--json`
   (e.g. with `--matches 1`) for a replay, or only `--stats-json` for the statistics.
   Keep `--seed` to regenerate exactly the same files.
2. Open `web/index.html` in a browser (double-click), then drag and drop `partie.jsonl` and/or
   `stats.json` onto the page (or use the file picker), or click **Exemples** to see the bundled sample.
3. Replay: Hearthstone-like board, hands, minions, heroes, mana, statuses, the same log as the console,
   play/pause, step by event or by turn (keys: Space, ←/→, Shift+←/→, Home/End), speed, scrubber,
   synthesized sounds for each event (Web Audio, no audio files; 🔊 button or key M, volume slider),
   an original epic music loop that follows the match (calm at setup, drums in battle, a heroic melody
   when a champion is at 10 HP or less; 🎵 button or key B),
   animations (card reveal, spell projectiles, minion attacks, impacts and screen shake, summons, deaths,
   auras, turn banner, victory), card icons, full card text on hover, and an HP chart (click to jump).
   Statistics: win split, first-player advantage, match lengths, end reasons, and a table of every match.
4. Front tests: open `web/tests.html` in Chrome/Edge/Firefox (not in an editor preview; no Node needed). With Node:
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
| `bots.llm` | `LlmBot`, `LlmDeckStrategy`, `LlmClient` and its Claude implementation |
| `log`, `json` | console renderer and JSON exporter (both event listeners), minimal JSON writer |
| `cli`, `stats` | command line, match runner, stats and stats JSON export |
| `web/` | HTML/CSS/JS replay and statistics (reads the JSON exports) |

Game summary in French (rules, classes, cards, bots): [`docs/RESUME-DU-JEU.md`](docs/RESUME-DU-JEU.md).

Team docs: [`CLAUDE.md`](CLAUDE.md), [`docs/TEAM_GUIDELINE.md`](docs/TEAM_GUIDELINE.md),
[`docs/00-PHASE-0-DECISIONS.md`](docs/00-PHASE-0-DECISIONS.md), [`docs/plans/`](docs/plans/).
