package com.arena.cli;

import com.arena.engine.classes.ClassDecks;
import com.arena.engine.classes.HeroClass;

/** Creates a {@link HeroClass} by name; adding a new class only means adding one case here. */
public final class ClassFactory {

    private ClassFactory() {
    }

    public static HeroClass create(String name) {
        return switch (name) {
            case "Tank" -> ClassDecks.tank();
            default -> ClassDecks.mage();
        };
    }
}
