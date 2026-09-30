# Skirmish Arena

## Rules for this project
- This is a 100% vibecoding exercise — write nothing by hand, express every
  change as a prompt. Always show me the diff before I approve it.
- Plain Java 21, Maven, no frameworks (JUnit 5 + AssertJ for tests only).
  No UI, no web layer, no database.
- Strict TDD: write one failing test, run it and show it fails (red), write
  the minimal code to pass (green), then refactor. No production code
  without a failing test first.
- Tests follow Given/When/Then: method name `given_<context>_when_<action>_then_<outcome>`
  and `// Given`, `// When`, `// Then` blocks. One behaviour per test.
  No Mockito — use small hand-written fakes (fixed deck, seeded Random, fake bot).
- Run `mvn test` before considering any task done — don't tell me something
  works without having run it. Paste the test summary line in your answer.
- DESIGN.md is the source of truth for rules, classes and card numbers;
  record any new decision there in the same change.
- Work on one task of the backlog (`docs/plans/`) at a time. Changing a
  shared contract from sprint 0 needs team agreement: stop and ask. Team
  workflow (mob, one PC): `docs/TEAM_GUIDELINE.md`; decisions:
  `docs/00-PHASE-0-DECISIONS.md`. Suggest a commit after each green step.

## Architecture
- Root package `com.arena`; entry point `com.arena.Main`
  (`--matches N --p1 <Bot>:<Class|auto> --p2 <Bot>:<Class|auto> [--seed S] [--log] [--preset-decks]`)
- Hearthstone-like: 30 HP, 20-card class decks, a hero power per class,
  minions on a board (max 5, Taunt). Categories: Attack, Defense, Resource, Utility.
- Turn phases: draw → mana → play → resolve → end. Mana +1/turn, cap 10.
  Match ends at 0 HP or after 50 turns (tie-break in DESIGN.md).
- The engine never prints. It publishes `GameEvent`s to `GameEventListener`s;
  console log, stats and a future JSON/HTML front are all listeners.
- Bots are pure strategies: they read a `GameView` and return an `Action`. No IO.
- Card effects are small reusable building blocks; a card or hero power is
  a composition of them. All randomness uses an injected, seeded `Random`.

## SOLID, concretely
- S: turn loop, damage resolution, bots, rendering and stats are separate classes.
- O: add a card, class, bot or event by adding a class, not by editing a switch.
- L: any `Bot`, `Effect` or `GameEventListener` works wherever the interface is expected.
- I: bots see a read-only `GameView`, never the mutable game state.
- D: the engine depends on interfaces (`Bot`, `GameEventListener`, `DamageResolver`,
  `Random`); concrete classes are wired in `Main`.

## Conventions
- Package by feature, not by layer (`engine.combat`, `engine.cards`, not `models` / `services`)
- Every public method needs a one-line Javadoc explaining *why*, not *what*
