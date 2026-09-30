---
name: run-simulation
description: Runs N simulated matches between two bot/class pairings and prints win rate, average match length, and average damage. Use when asked to test, run, benchmark or balance the engine, or to compare bots or classes.
allowed-tools: Bash(mvn:*)
---

Run the simulation with:
!`mvn -q compile exec:java -Dexec.mainClass=com.arena.Main -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 42"`

If the user asked for other bots, classes, seed or number of matches,
re-run with those arguments instead (format `<Bot>:<Class>`, e.g.
`Aggressive:Assassin`). Add `--log` to print the sample match log.

Summarize the output: win rate per side, draw rate, first-player win rate,
average match length, and average damage per match. Flag anything that
looks off:
- a win rate near 0% or 100% (usually a bug, not a balanced matchup)
- a first-player win rate far from 50% (The Coin may need tuning)
- most matches hitting the 50-turn limit (damage too low / armor too strong)
- average match length under ~5 turns (damage too high)
If the build fails, report the error instead of guessing the numbers.
When suggesting balance changes, name the card and the new number, and
remind the user to update DESIGN.md.
