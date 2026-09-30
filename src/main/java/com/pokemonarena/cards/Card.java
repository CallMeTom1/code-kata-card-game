package com.pokemonarena.cards;

/**
 * Immutable definition of a card. Cards are pure data plus effects; the mutable in-play state
 * (current HP, attack availability, ...) lives in the board and hero packages.
 * <p>
 * Ids are stable lowercase technical identifiers (aligned with PokéAPI slugs for Pokémon) and
 * never depend on the display name.
 */
public sealed interface Card permits PokemonCard, ItemCard {

    String id();

    String name();

    int manaCost();

    CardNature nature();

    CardCategory category();

    /** Card text shown in the match log; empty when the card has no effect of its own. */
    String text();
}
