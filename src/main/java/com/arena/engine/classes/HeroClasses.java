package com.arena.engine.classes;

import java.util.List;
import java.util.Optional;

/** Looks classes up by the name typed on the command line; new classes are added in one place. */
public final class HeroClasses {

    private final List<HeroClass> classes;

    /** Takes the list from outside so tests and Main choose which classes exist. */
    public HeroClasses(List<HeroClass> classes) {
        this.classes = List.copyOf(classes);
    }

    /** Case-insensitive, because users type "mage" as often as "Mage". */
    public Optional<HeroClass> byName(String name) {
        return classes.stream().filter(c -> c.name().equalsIgnoreCase(name)).findFirst();
    }

    /** Every registered class, e.g. for a bot that picks its own. */
    public List<HeroClass> all() {
        return classes;
    }
}
