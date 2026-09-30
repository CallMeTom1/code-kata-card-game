# Skirmish Arena

## Rules for this project
- This is a 100% vibecoding exercise — write nothing by hand, express every
  change as a prompt. Always show me the diff before I approve it.
- Plain Java 21, Maven, no frameworks. No UI, no web layer, no database.
- Run `mvn test` before considering any task done — don't tell me something
  works without having run it. Paste the test summary line in your answer.
- When you make a design decision the brief left open, record it in
  DESIGN.md in the same change. DESIGN.md is the source of truth for rules,
  classes and card numbers.
- Suggest a git commit after each working step; keep commits small.

## Architecture
- Root package `com.arena`; entry point `com.arena.Main`
  (`--matches N --p1 <Bot>:<Class> --p2 <Bot>:<Class> [--seed S] [--log]`)
- Hearthstone-like: 30 HP, 20-card class decks (class + neutral cards), a
  hero power per class, minions on a board (max 5, Taunt). Categories:
  Attack, Defense, Resource, Utility.
- Turn phases: draw → mana → play → resolve → end. Mana +1/turn, cap 10.
  Match ends at 0 HP or after 50 turns (tie-break in DESIGN.md).
- Bots are pure strategy objects — no bot should touch UI/IO code
- Card effects are small reusable building blocks (damage, armor, heal,
  draw, mana, poison, summon…); a card or hero power is a composition of them.
- All randomness goes through an injected, seeded `java.util.Random`.
- The turn-by-turn log is separate from engine logic.

## Conventions
- Package by feature, not by layer (`engine.combat`, `engine.cards`, not
  `models` / `services`)
- Every public method needs a one-line Javadoc explaining *why*, not *what*
- JUnit 5; every card effect, hero power and bot has at least one test.
