Pokémon Skirmish Arena
Project rules

100% vibecoding: never manually write or edit code. Every change must be requested through a prompt.

Plain Java + Maven only. No frameworks, UI, web server, database, or external API in the engine.

Read DESIGN.md before changing gameplay rules, cards, Heroes, or combat.

Always show the diff after changes.

Always run mvn test after code changes. Never claim a task is complete without successful tests.

Keep the code simple, readable, testable, and easy to explain.

Do not add features outside the design unless explicitly requested.

Game concept

Hearthstone-inspired Pokémon card battler.

Two Heroes fight each other, each starting with 30 HP.

Heroes: Electhor, Artikodin, Sulfura, Lugia, Ho-Oh.

Each Hero has a unique Hero Power and a 20-card deck.

Pokémon cards are permanent board characters.

Item cards have immediate effects and go to the discard pile.

Maximum 3 Pokémon on each board.

Architecture

Organize by feature: hero, cards, board, deck, game, bot, simulation, logging.

Keep game rules inside the engine.

Bots choose actions but never directly mutate game state.

Use Strategy for Bots.

Use small effect objects for card and Hero Power effects where useful.

Prefer composition and small interfaces over inheritance.

Isolate randomness and make it injectable for deterministic tests.

Card model

Every card has a stable ID, name, mana cost, nature, and gameplay category.

Card nature is POKEMON or ITEM.

Gameplay category is ATTACK, DEFENSE, RESOURCE, or UTILITY.

Pokémon have attack, HP, and board state.

Items resolve immediately and normally enter the discard pile.

Do not create a separate rules engine for Pokémon and Items.

Combat

Pokémon can attack opposing Pokémon or the opposing Hero.

Pokémon cannot attack on the same turn they are played.

Pokémon remain on the board until their HP reaches 0.

Dead Pokémon leave the board and enter the discard pile.

Heroes can be attacked directly by Pokémon.

The match ends when a Hero reaches 0 HP.

Frontend boundary

The engine must remain independent from PokéAPI.

A future frontend will use PokéAPI for artwork, sprites, types, and Pokémon metadata.

Use stable Pokémon identifiers for frontend mapping.

Never add HTTP/JSON/PokéAPI dependencies unless explicitly requested.

Quality

Test game rules, card effects, Pokémon combat, Hero Powers, Bot decisions, and simulation statistics.

Every deterministic bug should be reproducible with a seed.

Avoid generic utils, helpers, managers, and unnecessary services.