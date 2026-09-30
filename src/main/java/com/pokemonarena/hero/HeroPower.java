package com.pokemonarena.hero;

import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.cards.effects.Effect;

import java.util.Objects;

/**
 * A Hero Power: a domain object, not a hard-coded branch of the turn engine. It is not part of
 * the deck and can be used at most once per turn.
 */
public record HeroPower(String id,
                        String name,
                        int manaCost,
                        CardCategory category,
                        Effect effect) {

    public HeroPower {
        Objects.requireNonNull(effect, "a Hero Power must have an effect: " + id);
        if (manaCost < 0) {
            throw new IllegalArgumentException("mana cost cannot be negative: " + id);
        }
    }

    /** Human-readable description, used by the match log. */
    public String text() {
        String description = effect.describe();
        return Character.toUpperCase(description.charAt(0)) + description.substring(1) + ".";
    }
}
